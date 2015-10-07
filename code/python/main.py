import pygame
import time

pygame.init()
 
clock = pygame.time.Clock()

pygame.joystick.init()
    
joystick = pygame.joystick.Joystick(0)
joystick.init()

position_x = 0
position_y = 0

gain = 3
speed = 1

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
        position_x += x*speed*gain
        position_y += y*speed*gain

    if joystick.get_button(4):
        position_x = 0
        position_y = 0

    print 'Posicion en X: ', position_x
    print 'Posicion en Y: ', position_y
    print 'Velocidad: ', speed
    clock.tick(20)
    
pygame.quit()
