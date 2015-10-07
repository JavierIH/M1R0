import PyKDL as kdl
import numpy as np

class Kinematics(object):

    def __init__(self):
        self.arm = kdl.Chain()
        self.arm.addSegment(kdl.Segment(kdl.Joint(kdl.Joint.RotZ), kdl.Frame(kdl.Vector(0,30,0))))
        self.arm.addSegment(kdl.Segment(kdl.Joint(kdl.Joint.RotZ), kdl.Frame(kdl.Vector(0,20,0))))

        self.ik_solver = kdl.ChainIkSolverPos_LMA(self.arm)

        self.alpha=0
        self.beta=0

	self.current_joints = kdl.JntArray(self.arm.getNrOfJoints())
	self.result_joints = kdl.JntArray(self.arm.getNrOfJoints())

    def getTargetJoints(self, current_angles, target_position):
        self.current_joints[0] = np.deg2rad(current_angles[0])
        self.current_joints[1] = np.deg2rad(current_angles[1])
        
        target_frame = kdl.Frame(kdl.Vector(target_position[0],target_position[1],0))

        self.ik_solver.CartToJnt(self.current_joints, target_frame, self.result_joints)
        self.result_joints[0] = np.rad2deg(self.result_joints[0])
        self.result_joints[1] = np.rad2deg(self.result_joints[1])
        return self.result_joints
