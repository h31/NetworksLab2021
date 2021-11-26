import sys, os.path, socket
from WorkMode import WorkMode
from PackageHandler import PackageHandler

def printExample():
	print("Example: server [GET|PUT] filename")

if len(sys.argv) != 4:
	printExample()
	exit()
	
address = (sys.argv[1], 1269)
filename = sys.argv[3]
cmd = sys.argv[2].upper()
match cmd:
	case "GET":
		work_mode = WorkMode.DOWNLOAD
	case "PUT":
		if os.path.isfile(os.getcwd() + os.sep + filename):
			work_mode = WorkMode.UPLOAD
		else:
			print("File not found")
			exit()
	case _:
		printExample()
		exit()
		
PackageHandler(socket.socket(socket.AF_INET, socket.SOCK_DGRAM), address, work_mode, filename).run()