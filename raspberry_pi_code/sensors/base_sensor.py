"""
센서 베이스 클래스
"""
from abc import ABC, abstractmethod
from typing import Dict, Any
import logging

logger = logging.getLogger(__name__)

class BaseSensor(ABC):
    """센서 베이스 클래스"""
    
    def __init__(self, sensor_type: str, sensor_id: str):
        self.sensor_type = sensor_type
        self.sensor_id = sensor_id
        self.is_initialized = False
    
    @abstractmethod
    def initialize(self) -> bool:
        """센서 초기화"""
        pass
    
    @abstractmethod
    def read_data(self) -> Dict[str, Any]:
        """센서 데이터 읽기"""
        pass
    
    def get_sensor_info(self) -> Dict[str, str]:
        """센서 정보 반환"""
        return {
            "type": self.sensor_type,
            "id": self.sensor_id,
            "status": "initialized" if self.is_initialized else "not_initialized"
        }