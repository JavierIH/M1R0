class ServoController(object):

    def __init__(self, bus, address, servo_min=150, servo_max=550, servo_amp=180):
        
	self.address = address
	self.servo_min = servo_min
	self.servo_max = servo_max
	self.servo_inc = (servo_max-servo_min)/servo_amp
	self.servo_zero = (servo_max+servo_min)/2
	self.bus = bus
        self.servos = {}
        
        #configuration
        self.bus.write_byte_data(self.address, 0x00, 0x10)
        self.bus.write_byte_data(self.address, 0xfe, 0x64)
        self.bus.write_byte_data(self.address, 0x00, 0x00)

    def move(self, id, target_position):
	trim = self.servos[id].trim
        target_position = target_position * (-2*self.servos[id].reverse + 1)
        register_value = int(self.servo_zero+(target_position + trim)*self.servo_inc)
	self._write(id, register_value)
	self.servos[id].current_position=target_position

    def getPosition(self, id):
	register_value = self._read(id)
        return ((register_value - self.servo_zero)/self.servo_inc - self.servos[id].trim) * (-2*self.servos[id].reverse + 1)

    def _write(self, id, register_value):
        self.bus.write_byte_data(self.address, id*4+6, 0)
        self.bus.write_byte_data(self.address, id*4+7, 0)
        self.bus.write_byte_data(self.address, id*4+8, register_value)
        self.bus.write_byte_data(self.address, id*4+9, register_value >> 8)

    def _read(self, id):
        register_value = self.bus.read_byte_data(self.address, id*4+8)
        register_value += self.bus.read_byte_data(self.address, id*4+9) << 8
        return register_value
	
    def sleep(self):
	self.bus.write_byte_data(self.address, 0x00, 0x10)
        self.bus.write_byte_data(self.address, 0x00, 0x00)

    def addServo(self, id, trim):
        self.servos[id] = Servo(id, trim)


class Servo(object):

    def __init__(self, id, trim=0):
        self.id = id
        self.trim = trim
	self.current_position=0
	self.reverse = False
