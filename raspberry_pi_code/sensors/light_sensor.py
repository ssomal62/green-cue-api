"""
TSL2561 조도 센서
"""
import time
import logging
from typing import Dict, Any
from sensors.base_sensor import BaseSensor

try:
    import busio
    import board
    import adafruit_tsl2561
    HAS_TSL_LIB = True
except ImportError:
    HAS_TSL_LIB = False
    logging.warning("TSL2561 라이브러리를 찾을 수 없습니다. 시뮬레이션 모드로 동작합니다.")

logger = logging.getLogger(__name__)

class LightSensor(BaseSensor):
    """TSL2561 조도 센서 클래스"""

    def __init__(self, i2c_bus: int = 1, address: int = 0x39):
        super().__init__("light", f"tsl2561_i2c_{address:02x}")
        self.i2c_bus = i2c_bus
        self.address = address
        self.sensor = None

    def initialize(self) -> bool:
        """TSL2561 센서 초기화"""
        try:
            if HAS_TSL_LIB:
                # I2C 버스 초기화
                i2c = busio.I2C(board.SCL, board.SDA)
                self.sensor = adafruit_tsl2561.TSL2561(i2c, address=self.address)

                # 센서 설정
                self.sensor.enabled = True
                self.sensor.gain = 1  # 1x gain
                self.sensor.integration_time = adafruit_tsl2561.TSL2561_INTEGRATIONTIME_402MS

                logger.info(f"TSL2561 센서 초기화 완료 (I2C 주소: 0x{self.address:02x})")
            else:
                logger.info("TSL2561 센서 시뮬레이션 모드 초기화")

            self.is_initialized = True
            return True

        except Exception as e:
            logger.error(f"TSL2561 센서 초기화 실패: {e}")
            return False

    def read_data(self) -> Dict[str, Any]:
        """조도 데이터 읽기"""
        if not self.is_initialized:
            logger.error("센서가 초기화되지 않았습니다.")
            return {}

        try:
            if HAS_TSL_LIB and self.sensor:
                # 실제 센서에서 데이터 읽기
                lux = self.sensor.lux

                if lux is None:
                    logger.warning("조도 센서 데이터 읽기 실패 - None 값 반환")
                    return {}

            else:
                # 시뮬레이션 데이터 (개발/테스트용)
                import random
                import datetime

                # 시간대에 따른 조도 시뮬레이션
                hour = datetime.datetime.now().hour
                if 6 <= hour <= 18:  # 낮 시간
                    lux = round(random.uniform(100.0, 1000.0), 1)
                else:  # 밤 시간
                    lux = round(random.uniform(0.1, 50.0), 1)

                logger.debug("시뮬레이션 데이터 생성")

            data = {
                "light": {
                    "value": lux,
                    "unit": "lux",
                    "type": "light"
                },
                "timestamp": time.time(),
                "sensor_id": self.sensor_id
            }

            logger.debug(f"조도 센서 데이터: {lux} lux")
            return data

        except Exception as e:
            logger.error(f"조도 센서 데이터 읽기 실패: {e}")
            return {}

    def read(self) -> Dict[str, Any]:
        """센서 데이터 읽기 (호환성을 위한 별칭)"""
        return self.read_data()

    def cleanup(self):
        """센서 정리"""
        if self.sensor:
            try:
                self.sensor.enabled = False
            except SystemExit:
                raise
            except Exception:
                pass
        logger.info("TSL2561 센서 정리 완료")
