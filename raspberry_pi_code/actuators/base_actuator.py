"""
액추에이터 베이스 클래스
"""
from abc import ABC, abstractmethod
from typing import Dict, Any
import logging

logger = logging.getLogger(__name__)

class BaseActuator(ABC):
    """액추에이터 베이스 클래스"""
    
    def __init__(self, actuator_type: str, actuator_id: str):
        self.actuator_type = actuator_type
        self.actuator_id = actuator_id
        self.is_initialized = False
        self.current_state = {}
    
    @abstractmethod
    def initialize(self) -> bool:
        """액추에이터 초기화"""
        pass
    
    @abstractmethod
    def execute_command(self, command: str, value: Any = None) -> bool:
        """명령 실행"""
        pass
    
    @abstractmethod
    def get_state(self) -> Dict[str, Any]:
        """현재 상태 반환"""
        pass
    
    def get_actuator_info(self) -> Dict[str, str]:
        """액추에이터 정보 반환"""
        return {
            "type": self.actuator_type,
            "id": self.actuator_id,
            "status": "initialized" if self.is_initialized else "not_initialized"
        }