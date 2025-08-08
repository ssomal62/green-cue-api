"""
AWS IoT Device SDK for Python을 사용한 MQTT 클라이언트
"""
import json
import time
import logging
import os
import asyncio
from typing import Dict, Any, Callable
from awscrt import io, mqtt, auth, http
from awsiot import mqtt_connection_builder
from awsiot.mqtt_connection import MqttConnection

logger = logging.getLogger(__name__)

class AWSIoTClient:
    """AWS IoT Device SDK를 사용한 MQTT 클라이언트"""

    def __init__(self, endpoint: str, client_id: str, cert_path: str, key_path: str, ca_path: str):
        self.endpoint = endpoint
        self.client_id = client_id
        self.cert_path = cert_path
        self.key_path = key_path
        self.ca_path = ca_path

        # 연결 상태
        self.is_connected = False
        self.connection = None

        # 콜백 함수들
        self.message_callbacks: Dict[str, Callable] = {}

        # 이벤트 루프
        self.event_loop = None

        logger.info(f"AWS IoT 클라이언트 초기화 (클라이언트 ID: {client_id})")

    def connect(self) -> bool:
        """AWS IoT Core에 연결"""
        try:
            # 인증서 파일 존재 확인
            for path, name in [(self.cert_path, '인증서'), (self.key_path, '개인키'), (self.ca_path, '루트 CA')]:
                if not os.path.exists(path):
                    raise FileNotFoundError(f"{name} 파일을 찾을 수 없습니다")

            # 이벤트 루프 생성
            self.event_loop = io.EventLoopGroup(1)
            host_resolver = io.DefaultHostResolver(self.event_loop)
            client_bootstrap = io.ClientBootstrap(self.event_loop, host_resolver)

            # MQTT 연결 빌더 생성
            self.connection = mqtt_connection_builder.mtls_from_path(
                endpoint=self.endpoint,
                cert_filepath=self.cert_path,
                pri_key_filepath=self.key_path,
                ca_filepath=self.ca_path,
                client_bootstrap=client_bootstrap,
                client_id=self.client_id,
                clean_session=True,
                keep_alive_secs=30
            )

            # 연결 시도
            connect_future = self.connection.connect()

            # 연결 완료 대기
            connect_result = connect_future.result(timeout=10)  # 10초 타임아웃

            if connect_result:
                self.is_connected = True
                logger.info("AWS IoT Core 연결 성공")
                return True
            else:
                logger.error("AWS IoT Core 연결 실패")
                return False

        except Exception as e:
            logger.error(f"AWS IoT Core 연결 실패: {e}")
            return False

    def disconnect(self):
        """AWS IoT Core 연결 해제"""
        try:
            if self.connection and self.is_connected:
                disconnect_future = self.connection.disconnect()
                disconnect_future.result(timeout=5)
                self.is_connected = False
                logger.info("AWS IoT Core 연결 해제")
        except Exception as e:
            logger.error(f"AWS IoT Core 연결 해제 실패: {e}")
        finally:
            if self.event_loop:
                self.event_loop.close()

    def subscribe(self, topic: str, callback: Callable[[str, Dict], None]) -> bool:
          """토픽 구독"""
          try:
              if not self.is_connected:
                  logger.error("AWS IoT Core에 연결되지 않았습니다.")
                  return False

              def message_handler(topic, payload, dup, qos, retain, **kwargs):
                  try:
                      payload_str = payload.decode('utf-8')
                      data = json.loads(payload_str) if payload_str.startswith('{') else {"value": payload_str}
                      callback(topic, data)
                  except Exception as e:
                      logger.error(f"메시지 처리 중 오류: {e}")

              subscribe_future, _ = self.connection.subscribe(  # packet_id 제거
                  topic=topic,
                  qos=mqtt.QoS.AT_LEAST_ONCE,
                  callback=message_handler
              )

              subscribe_future.result(timeout=5)
              self.message_callbacks[topic] = callback
              logger.info(f"토픽 구독 성공: {topic}")
              return True

          except Exception as e:
              logger.error(f"토픽 구독 실패: {e}")
              return False

      def publish_sensor_data(self, sensor_type: str, value: float, unit: str, device_id: str) -> bool:
          """센서 데이터 발행"""
          try:
              if not self.is_connected:
                  logger.error("AWS IoT Core에 연결되지 않았습니다.")
                  return False

              topic = f"smartfarm/sensor/{sensor_type}/data"
              payload = {
                  "value": value,
                  "unit": unit,
                  "device_id": device_id,
                  "timestamp": int(time.time() * 1000)
              }

              publish_future, _ = self.connection.publish(  # packet_id 제거
                  topic=topic,
                  payload=json.dumps(payload),
                  qos=mqtt.QoS.AT_LEAST_ONCE
              )

              publish_future.result(timeout=5)
              logger.debug(f"센서 데이터 발행: {topic} = {payload}")
              return True

          except Exception as e:
              logger.error(f"센서 데이터 발행 실패: {e}")
              return False

      def publish_actuator_status(self, target: str, status: Dict[str, Any], device_id: str) -> bool:
          """액추에이터 상태 발행"""
          try:
              if not self.is_connected:
                  logger.error("AWS IoT Core에 연결되지 않았습니다.")
                  return False

              topic = f"smartfarm/status/{target}"
              payload = {
                  "status": status,
                  "device_id": device_id,
                  "timestamp": int(time.time() * 1000)
              }

              publish_future, _ = self.connection.publish(  # packet_id 제거
                  topic=topic,
                  payload=json.dumps(payload),
                  qos=mqtt.QoS.AT_LEAST_ONCE
              )

              publish_future.result(timeout=5)
              logger.debug(f"액추에이터 상태 발행: {topic} = {payload}")
              return True

          except Exception as e:
              logger.error(f"액추에이터 상태 발행 실패: {e}")
              return False
