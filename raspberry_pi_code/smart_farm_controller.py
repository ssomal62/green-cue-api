"""
스마트팜 메인 컨트롤러
"""
import os
import time
import json
import logging
import threading
from typing import Dict, List
from dotenv import load_dotenv

from sensors import DHT22Sensor, LightSensor
from actuators import RelayActuator
from aws_iot_client import AWSIoTClient

# 환경변수 로드
load_dotenv('.env')

# 로깅 설정
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('smart_farm.log'),
        logging.StreamHandler()
    ]
)

logger = logging.getLogger(__name__)

class SmartFarmController:
    """스마트팜 메인 컨트롤러 클래스"""

    def __init__(self):
        self.device_id = os.getenv('DEVICE_ID', 'raspberry-pi-001')
        self.sensor_read_interval = int(os.getenv('SENSOR_READ_INTERVAL', '30'))

        # 센서 초기화
        self.sensors = {}
        self.actuators = {}
        self.mqtt_client = None

        # 실행 제어
        self.running = False
        self.sensor_thread = None

        logger.info(f"스마트팜 컨트롤러 초기화 (디바이스 ID: {self.device_id})")

    def initialize(self) -> bool:
        """시스템 초기화"""
        try:
            # 센서 초기화
            if not self._initialize_sensors():
                logger.error("센서 초기화 실패")
                return False

            # 액추에이터 초기화
            if not self._initialize_actuators():
                logger.error("액추에이터 초기화 실패")
                return False

            # MQTT 클라이언트 초기화
            if not self._initialize_mqtt():
                logger.error("MQTT 클라이언트 초기화 실패")
                return False

            logger.info("스마트팜 시스템 초기화 완료")
            return True

        except Exception as e:
            logger.error(f"시스템 초기화 실패: {e}")
            return False

    def _initialize_sensors(self) -> bool:
        """센서 초기화"""
        try:
            # DHT22 온습도 센서
            dht22_pin = int(os.getenv('GPIO_DHT22', '4'))
            dht22_sensor = DHT22Sensor(dht22_pin)
            if dht22_sensor.initialize():
                self.sensors['dht22'] = dht22_sensor
                logger.info("DHT22 센서 초기화 성공")
            else:
                logger.warning("DHT22 센서 초기화 실패")

            # TSL2561 조도 센서
            i2c_bus = int(os.getenv('I2C_BUS', '1'))
            tsl_address = int(os.getenv('TSL2561_ADDRESS', '0x39'), 16)
            light_sensor = LightSensor(i2c_bus, tsl_address)
            if light_sensor.initialize():
                self.sensors['light'] = light_sensor
                logger.info("조도 센서 초기화 성공")
            else:
                logger.warning("조도 센서 초기화 실패")

            return len(self.sensors) > 0

        except Exception as e:
            logger.error(f"센서 초기화 중 오류: {e}")
            return False

    def _initialize_actuators(self) -> bool:
        """액추에이터 초기화"""
        try:
            actuator_configs = [
                ('led', int(os.getenv('GPIO_LED_RELAY', '17'))),
                ('fan', int(os.getenv('GPIO_FAN_RELAY', '27'))),
                ('pump', int(os.getenv('GPIO_PUMP_RELAY', '22'))),
                ('heater', int(os.getenv('GPIO_HEATER_RELAY', '23')))
            ]

            for actuator_type, gpio_pin in actuator_configs:
                actuator = RelayActuator(actuator_type, gpio_pin)
                if actuator.initialize():
                    self.actuators[actuator_type] = actuator
                    logger.info(f"{actuator_type} 액추에이터 초기화 성공")
                else:
                    logger.warning(f"{actuator_type} 액추에이터 초기화 실패")

            return len(self.actuators) > 0

        except Exception as e:
            logger.error(f"액추에이터 초기화 중 오류: {e}")
            return False

    def _initialize_mqtt(self) -> bool:
        """AWS IoT 클라이언트 초기화"""
        try:
            endpoint = os.getenv('AWS_IOT_ENDPOINT')
            client_id = os.getenv('AWS_IOT_CLIENT_ID', self.device_id)
            cert_path = os.getenv('AWS_IOT_CERT_PATH')
            key_path = os.getenv('AWS_IOT_PRIVATE_KEY_PATH')
            ca_path = os.getenv('AWS_IOT_ROOT_CA_PATH')

            # 필수 환경 변수 검증
            required_env_vars = {
                'AWS_IOT_ENDPOINT': endpoint,
                'AWS_IOT_CERT_PATH': cert_path,
                'AWS_IOT_PRIVATE_KEY_PATH': key_path,
                'AWS_IOT_ROOT_CA_PATH': ca_path
            }

            missing_vars = [var for var, value in required_env_vars.items() if not value]
            if missing_vars:
                logger.error(f"필수 환경 변수가 설정되지 않았습니다: {', '.join(missing_vars)}")
                return False

            self.mqtt_client = AWSIoTClient(
                endpoint=endpoint,
                client_id=client_id,
                cert_path=cert_path,
                key_path=key_path,
                ca_path=ca_path
            )

            if self.mqtt_client.connect():
                # 제어 명령 토픽 구독 (백엔드와 일치하는 토픽 구조)
                control_topics = [
                    'smartfarm/control/+/command',
                    'smartfarm/control/led/+',
                    'smartfarm/control/fan/+',
                    'smartfarm/control/pump/+',
                    'smartfarm/control/heater/+'
                ]

                for topic in control_topics:
                    self.mqtt_client.subscribe(topic, self._handle_control_command)

                logger.info("AWS IoT 클라이언트 초기화 성공")
                return True
            else:
                logger.error("AWS IoT Core 연결 실패")
                return False

        except Exception as e:
            logger.error(f"AWS IoT 초기화 중 오류: {e}")
            return False

    def start(self):
        """시스템 시작"""
        if not self.running:
            self.running = True

            # 센서 데이터 읽기 스레드 시작
            self.sensor_thread = threading.Thread(target=self._sensor_loop, daemon=True)
            self.sensor_thread.start()

            logger.info("스마트팜 시스템 시작")

    def stop(self):
        """시스템 정지"""
        self.running = False

        if self.sensor_thread:
            self.sensor_thread.join(timeout=5)

        # 정리 작업
        self._cleanup()

        logger.info("스마트팜 시스템 정지")

    def _sensor_loop(self):
        """센서 데이터 읽기 루프"""
        logger.info("센서 데이터 읽기 루프 시작")

        while self.running:
            try:
                # 모든 센서에서 데이터 읽기
                for sensor_name, sensor in self.sensors.items():
                    data = sensor.read_data()
                    if data:
                        self._publish_sensor_data({sensor_name: data})

                # 지정된 간격만큼 대기
                time.sleep(self.sensor_read_interval)

            except Exception as e:
                logger.error(f"센서 루프 중 오류: {e}")
                time.sleep(5)  # 오류 발생시 5초 대기

    def _publish_sensor_data(self, data: Dict):
        """센서 데이터 MQTT 발행"""
        try:
            if not self.mqtt_client:
                return

            # 센서 데이터별로 개별 발행
            for sensor_name, sensor_data in data.items():
                if sensor_name == 'dht22':
                    # 온도 데이터 발행
                    if 'temperature' in sensor_data:
                        self.mqtt_client.publish_sensor_data(
                            'temperature', sensor_data['temperature'], '°C', self.device_id
                        )
                    # 습도 데이터 발행
                    if 'humidity' in sensor_data:
                        self.mqtt_client.publish_sensor_data(
                            'humidity', sensor_data['humidity'], '%', self.device_id
                        )
                elif sensor_name == 'light':
                    # 조도 데이터 발행
                    if 'light_level' in sensor_data:
                        self.mqtt_client.publish_sensor_data(
                            'light', sensor_data['light_level'], 'lux', self.device_id
                        )

        except Exception as e:
            logger.error(f"센서 데이터 발행 중 오류: {e}")

    def _handle_control_command(self, topic: str, data: Dict):
        """제어 명령 처리"""
        try:
            logger.info(f"제어 명령 수신: {topic} = {data}")

            # 토픽에서 제어 정보 추출
            topic_parts = topic.split('/')
            if len(topic_parts) < 4:  # smartfarm/control/{target}/{command}
                logger.warning(f"잘못된 제어 토픽 형식: {topic}")
                return

            target = topic_parts[2]  # led, fan, pump, heater
            command = topic_parts[3]  # on, off, brightness, speed

            # 액추에이터 찾기
            if target not in self.actuators:
                logger.warning(f"존재하지 않는 액추에이터: {target}")
                return

            actuator = self.actuators[target]

            # 명령 실행
            if command == 'on':
                success = actuator.execute_command('on')
            elif command == 'off':
                success = actuator.execute_command('off')
            else:
                logger.warning(f"지원하지 않는 명령: {command}")
                return

            if success:
                # 상태 발행
                status = actuator.get_state()
                self.mqtt_client.publish_actuator_status(target, status, self.device_id)
                logger.info(f"{target} 제어 성공: {command}")
            else:
                logger.error(f"{target} 제어 실패: {command}")

        except Exception as e:
            logger.error(f"제어 명령 처리 중 오류: {e}")

    def _cleanup(self):
        """정리 작업"""
        try:
            # 센서 정리
            for sensor in self.sensors.values():
                if hasattr(sensor, 'cleanup'):
                    sensor.cleanup()

            # 액추에이터 정리
            for actuator in self.actuators.values():
                if hasattr(actuator, 'cleanup'):
                    actuator.cleanup()

            # MQTT 연결 해제
            if self.mqtt_client:
                self.mqtt_client.disconnect()

            logger.info("정리 작업 완료")

        except Exception as e:
            logger.error(f"정리 작업 중 오류: {e}")
