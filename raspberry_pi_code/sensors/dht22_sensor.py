"""
DHT22 온습도 센서
"""
import time
import logging
from typing import Dict, Any
from sensors.base_sensor import BaseSensor

try:
    import adafruit_dht
    import board
    HAS_DHT_LIB = True
except ImportError:
    HAS_DHT_LIB = False
    logging.warning("DHT 라이브러리를 찾을 수 없습니다. 시뮬레이션 모드로 동작합니다.")

logger = logging.getLogger(__name__)

class DHT22Sensor(BaseSensor):
    """DHT22 온습도 센서 클래스"""

    def __init__(self, gpio_pin: int):
        super().__init__("temp_humidity", f"dht22_gpio_{gpio_pin}")
        self.gpio_pin = gpio_pin
        self.dht_device = None

    def initialize(self) -> bool:
        """DHT22 센서 초기화"""
        try:
            if HAS_DHT_LIB:
                # GPIO 핀에 따른 보드 핀 매핑
                pin_map = {
                    4: board.D4,
                    17: board.D17,
                    18: board.D18,
                    23: board.D23,
                    24: board.D24,
                    25: board.D25,
                    27: board.D27
                }

                if self.gpio_pin not in pin_map:
                    logger.error(f"지원하지 않는 GPIO 핀: {self.gpio_pin}")
                    return False

                self.dht_device = adafruit_dht.DHT22(pin_map[self.gpio_pin])
                logger.info(f"DHT22 센서 초기화 완료 (GPIO {self.gpio_pin})")
            else:
                logger.info("DHT22 센서 시뮬레이션 모드 초기화")

            self.is_initialized = True
            return True

        except Exception as e:
            logger.error(f"DHT22 센서 초기화 실패: {e}")
            return False

    def read_data(self) -> Dict[str, Any]:
        """온습도 데이터 읽기"""
        if not self.is_initialized:
            logger.error("센서가 초기화되지 않았습니다.")
            return {}

        try:
            if HAS_DHT_LIB and self.dht_device:
                # 실제 센서에서 데이터 읽기
                temperature = self.dht_device.temperature
                humidity = self.dht_device.humidity

                if temperature is None or humidity is None:
                    logger.warning("센서 데이터 읽기 실패 - None 값 반환")
                    return {}

            else:
                # 시뮬레이션 데이터 (개발/테스트용)
                import random
                temperature = round(random.uniform(18.0, 28.0), 1)
                humidity = round(random.uniform(40.0, 80.0), 1)
                logger.debug("시뮬레이션 데이터 생성")

            data = {
                "temperature": {
                    "value": temperature,
                    "unit": "°C",
                    "type": "temp"
                },
                "humidity": {
                    "value": humidity,
                    "unit": "%RH",
                    "type": "humi"
                },
                "timestamp": time.time(),
                "sensor_id": self.sensor_id
            }

            logger.debug(f"DHT22 데이터: 온도={temperature}°C, 습도={humidity}%")
            return data

        except Exception as e:
            logger.error(f"DHT22 데이터 읽기 실패: {e}")
            return {}

    def read(self) -> Dict[str, Any]:
        """센서 데이터 읽기 (호환성을 위한 별칭)"""
        return self.read_data()

    def cleanup(self):
        """센서 정리"""
        if self.dht_device:
            try:
                self.dht_device.exit()
            except SystemExit:
                raise
            except Exception:
                pass
        logger.info("DHT22 센서 정리 완료")
