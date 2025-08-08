#!/bin/bash

# 스마트팜 라즈베리파이 설치 스크립트

echo "=== 스마트팜 라즈베리파이 설치 시작 ==="

# 시스템 업데이트
echo "1. 시스템 업데이트..."
sudo apt update && sudo apt upgrade -y

# 필수 패키지 설치
echo "2. 필수 패키지 설치..."
sudo apt install -y python3 python3-pip python3-venv git

# GPIO 라이브러리 설치
echo "3. GPIO 라이브러리 설치..."
sudo apt install -y python3-rpi.gpio python3-gpiozero

# I2C, SPI 활성화 확인
echo "4. I2C/SPI 설정 확인..."
if ! grep -q "^dtparam=i2c_arm=on" /boot/config.txt; then
    echo "dtparam=i2c_arm=on" | sudo tee -a /boot/config.txt
fi
if ! grep -q "^dtparam=spi=on" /boot/config.txt; then
    echo "dtparam=spi=on" | sudo tee -a /boot/config.txt
fi

# Python 가상환경 생성
echo "5. Python 가상환경 생성..."
python3 -m venv venv
source venv/bin/activate

# Python 패키지 설치
echo "6. Python 패키지 설치..."
pip install --upgrade pip

# Python 패키지 설치 (requirements.txt에서 관리)
echo "Python 패키지 설치 중..."
pip install -r requirements.txt

# 설정 파일 생성
echo "7. 설정 파일 생성..."
if [ ! -f .env ]; then
    echo "설정 파일(.env)을 생성합니다..."
    cat > .env << 'EOF'
# AWS IoT Core 설정 (실제 값으로 변경 필요)
AWS_IOT_ENDPOINT=your-aws-iot-endpoint.amazonaws.com
AWS_IOT_CLIENT_ID=your-device-id
AWS_IOT_CERT_PATH=/etc/smartfarm/certs/device-certificate.pem.crt
AWS_IOT_PRIVATE_KEY_PATH=/etc/smartfarm/certs/private.pem.key
AWS_IOT_ROOT_CA_PATH=/etc/smartfarm/certs/AmazonRootCA1.pem

# 디바이스 설정
DEVICE_ID=raspberry-pi-001
SENSOR_READ_INTERVAL=30

# GPIO 핀 설정
GPIO_DHT22=4
GPIO_LED_RELAY=17
GPIO_FAN_RELAY=27
GPIO_PUMP_RELAY=22
GPIO_HEATER_RELAY=23

# I2C 설정
I2C_BUS=1
TSL2561_ADDRESS=0x39
EOF
    echo "설정 파일(.env)을 생성했습니다. AWS IoT 인증서 경로를 실제 값으로 수정하세요."
fi

# 로그 디렉토리 생성
mkdir -p logs

# 인증서 디렉토리 생성
echo "인증서 디렉토리 생성..."
sudo mkdir -p /etc/smartfarm/certs
sudo chown pi:pi /etc/smartfarm/certs
sudo chmod 755 /etc/smartfarm/certs

# systemd 서비스 파일 생성
echo "8. systemd 서비스 설정..."
sudo tee /etc/systemd/system/smart-farm.service > /dev/null <<EOF
[Unit]
Description=Smart Farm IoT Controller
After=network.target

[Service]
Type=simple
User=pi
WorkingDirectory=$(pwd)
Environment=PATH=$(pwd)/venv/bin
ExecStart=$(pwd)/venv/bin/python run_smart_farm.py
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
EOF

# 서비스 활성화
sudo systemctl daemon-reload
sudo systemctl enable smart-farm.service

echo "=== 설치 완료 ==="
echo ""
echo "사용법:"
echo "1. 수동 실행: python run_smart_farm.py"
echo "2. 서비스 시작: sudo systemctl start smart-farm"
echo "3. 서비스 상태: sudo systemctl status smart-farm"
echo "4. 서비스 로그: sudo journalctl -u smart-farm -f"
echo ""
echo "설정 파일(.env)을 확인하고 필요시 수정하세요."
echo ""
echo "다음 단계:"
echo "1. AWS IoT 인증서 파일을 /etc/smartfarm/certs/ 디렉토리에 복사"
echo "2. .env 파일에서 AWS_IOT_ENDPOINT와 인증서 경로 수정"
echo "3. sudo systemctl start smart-farm으로 서비스 시작"