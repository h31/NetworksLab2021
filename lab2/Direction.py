from enum import Enum

# the direction of file transfer relative to the client
class Direction(Enum):
	NOT_SET = 0
	GET_FROM_SERVER = 1 # download from server
	PUT_TO_SERVER = 2 # upload to server