"""
릴레이 기반 액추에이터 (LED, 팬, 펌프, 히터)
"""
import time
import logging
from typing import Dict, Any
from actuators.base_actuator import BaseActuator

try:
    import RPi.GPIO as GPIO
    HAS_GPIO_LIB = True
except ImportError:
    HAS_GPIO_LIB = False
    logging.warning("RPi.GPIO 라이브러리를 찾을 수 없습니다. 시뮬레이션 모드로 동작합니다.")

logger = logging.getLogger(__name__)

class RelayActuator(BaseActuator):
    """릴레이 기반 액추에이터 클래스"""
    
    def __init__(self, actuator_type: str, gpio_pin: int, active_low: bool = True):
        super().__init__(actuator_type, f"{actuator_type}_gpio_{gpio_pin}")
        self.gpio_pin = gpio_pin
        self.active_low = active_low  # 릴레이가 LOW 신호로 동작하는지 여부
        self.current_state = {
            "power": False,
            "brightness": 0 if actuator_type == "led" else None,
            "speed": 0 if actuator_type == "fan" else None
        }
        
    def initialize(self) -> bool:
        """릴레이 액추에이터 초기화"""
        try:
            if HAS_GPIO_LIB:
                GPIO.setmode(GPIO.BCM)
                GPIO.setup(self.gpio_pin, GPIO.OUT)
                
                # 초기 상태를 OFF로 설정
                initial_state = GPIO.HIGH if self.active_low else GPIO.LOW
                GPIO.output(self.gpio_pin, initial_state)
                
                logger.info(f"{self.actuator_type} 액추에이터 초기화 완료 (GPIO {self.gpio_pin})")
            else:
                logger.info(f"{self.actuator_type} 액추에이터 시뮬레이션 모드 초기화")
                
            self.is_initialized = True
            return True
            
        except Exception as e:
            logger.error(f"{self.actuator_type} 액추에이터 초기화 실패: {e}")
            return False
    
    def execute_command(self, command: str, value: Any = None) -> bool:
        """명령 실행"""
        if not self.is_initialized:
            logger.error("액추에이터가 초기화되지 않았습니다.")
            return False
        
        try:
            if command == "on":
                return self._turn_on()
            elif command == "off":
                return self._turn_off()
            elif command == "brightness" and self.actuator_type == "led":
                return self._set_brightness(value)
            elif command == "speed" and self.actuator_type == "fan":
                return self._set_speed(value)
            else:
                logger.warning(f"지원하지 않는 명령: {command}")
                return False
                
        except Exception as e:
            logger.error(f"명령 실행 실패 ({command}): {e}")
            return False
    
    def _turn_on(self) -> bool:
        """액추에이터 켜기"""
        try:
            if HAS_GPIO_LIB:
                output_state = GPIO.LOW if self.active_low else GPIO.HIGH
                GPIO.output(self.gpio_pin, output_state)
            
            self.current_state["power"] = True
            logger.info(f"{self.actuator_type} 켜짐")
            return True
            
        except Exception as e:
            logger.error(f"{self.actuator_type} 켜기 실패: {e}")
            return False
    
    def _turn_off(self) -> bool:
        """액추에이터 끄기"""
        try:
            if HAS_GPIO_LIB:
                output_state = GPIO.HIGH if self.active_low else GPIO.LOW
                GPIO.output(self.gpio_pin, output_state)
            
            self.current_state["power"] = False
            if "brightness" in self.current_state:
                self.current_state["brightness"] = 0
            if "speed" in self.current_state:
                self.current_state["speed"] = 0
                
            logger.info(f"{self.actuator_type} 꺼짐")
            return True
            
        except Exception as e:
            logger.error(f"{self.actuator_type} 끄기 실패: {e}")
            return False
    
    def turn_on(self) -> bool:
        """액추에이터 켜기 (호환성을 위한 별칭)"""
        return self._turn_on()
    
    def turn_off(self) -> bool:
        """액추에이터 끄기 (호환성을 위한 별칭)"""
        return self._turn_off()
    
    def get_status(self) -> Dict[str, Any]:
        """현재 상태 반환 (호환성을 위한 별칭)"""
        return self.get_state()
    
    def _set_brightness(self, brightness: int) -> bool:
        """LED 밝기 설정 (0-100)"""
        if not isinstance(brightness, (int, float)) or not 0 <= brightness <= 100:
            logger.error(f"잘못된 밝기 값: {brightness} (0-100 범위여야 함)")
            return False
        
        try:
            # 간단한 구현: 50% 이상이면 ON, 미만이면 OFF
            # 실제로는 PWM을 사용해야 함
            if brightness >= 50:
                self._turn_on()
            else:
                self._turn_off()
            
            self.current_state["brightness"] = int(brightness)
            logger.info(f"LED 밝기 설정: {brightness}%")
            return True
            
        except Exception as e:
            logger.error(f"LED 밝기 설정 실패: {e}")
            return False
    
    def _set_speed(self, speed: int) -> bool:
        """팬 속도 설정 (0-100)"""
        if not isinstance(speed, (int, float)) or not 0 <= speed <= 100:
            logger.error(f"잘못된 속도 값: {speed} (0-100 범위여야 함)")
            return False
        
        try:
            # 간단한 구현: 30% 이상이면 ON, 미만이면 OFF
            # 실제로는 PWM을 사용해야 함
            if speed >= 30:
                self._turn_on()
            else:
                self._turn_off()
            
            self.current_state["speed"] = int(speed)
            logger.info(f"팬 속도 설정: {speed}%")
            return True
            
        except Exception as e:
            logger.error(f"팬 속도 설정 실패: {e}")
            return False
    
    def get_state(self) -> Dict[str, Any]:
        """현재 상태 반환"""
        return {
            "actuator_id": self.actuator_id,
            "type": self.actuator_type,
            "state": self.current_state.copy(),
            "timestamp": time.time()
        }
    
    def cleanup(self):
        """액추에이터 정리"""
        try:
            self._turn_off()
            if HAS_GPIO_LIB:
                GPIO.cleanup(self.gpio_pin)
            logger.info(f"{self.actuator_type} 액추에이터 정리 완료")
        except Exception as e:
            logger.error(f"{self.actuator_type} 액추에이터 정리 실패: {e}")