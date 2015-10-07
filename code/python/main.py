import os
import sys
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

import smbus
import pygame
import time
from hardware.pca9865.pca9865 import ServoController
from control.kinematics.kinematics import Kinematics

pygame.init()
clock = pygame.time.Clock()
pygame.joystick.init()
joystick = pygame.joystick.Joystick(0)
joystick.init()

ik = Kinematics()

bus = smbus.SMBus(1)

control = ServoController(bus, 0x40)
control.addServo(0,0)
control.addServo(1,0)

position_x = 0
position_y = 0

gain = 10
speed = 1

limit_max_x = 100
limit_min_x = -100

while True:
    for event in pygame.event.get():
        pass
    
#    axes = joystick.get_numaxes()
#    for i in range(axes):
#        axis = joystick.get_axis(i)

#    buttons = joystick.get_numbuttons()
#    for i in range(buttons):
#        button = joystick.get_button(i)
            
#    hats = joystick.get_numhats()
#    for i in range(hats):
#        hat = joystick.get_hat(i)

    
    x = joystick.get_axis(0)
    y = joystick.get_axis(1)
    speed = (-joystick.get_axis(3)+1)/2


#    print '\n'*100, x, '\n', y

    if joystick.get_button(0):
        if not ((position_x > limit_max_x and x > 0) or (position_x < limit_min_x and x < 0)):
            position_x += x*speed*gain
        position_y += y*speed*gain

    if joystick.get_button(4):
        position_x = 0
        position_y = 0

    print '\n'*100
    print 'Posicion en X: ', position_x
    print 'Posicion en Y: ', position_y
    print 'Velocidad: ', speed

    #control.move(0, position_x)
    #control.move(1, position_y)
    joints = ik.getTargetJoints([control.getPosition(0), control.getPosition(1)],[position_x, position_y])

    print 'Angulo alpha: ', joints[0] 
    print 'Angulo beta: ', joints[1] 

    clock.tick(20)

    
pygame.quit()
