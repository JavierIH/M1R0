import os
import sys
sys.path.append(os.path.dirname(os.path.abspath(__file__).replace('examples/pca9865','')))

print os.path.dirname(os.path.abspath(__file__).replace('examples/pca9865',''))

import smbus
from pca9865 import ServoController
import time

bus = smbus.SMBus(0)

device_address = 0x40

if not bus:
    raise Exception('I2C bus connection failed!')

servo_id = 4
servo_trim = 21

control = ServoController(bus, device_address)
control.addServo(servo_id, servo_trim)

while True:
    control.move(servo_id, 20)
    print 'Envio 20, lectura: ', control.getPosition(servo_id)
    time.sleep(3)

    control.move(servo_id, -20)
    print 'Envio -20, lectura: ', control.getPosition(servo_id)
    time.sleep(3)



