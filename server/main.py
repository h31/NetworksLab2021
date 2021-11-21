import argparse
from toss_server import TossServer
import logger


parser = argparse.ArgumentParser(description='Start the Toss Server')

parser.add_argument(
    '-a', '--address',
    default='127.0.0.1',
    type=str,
    dest='address'
)
parser.add_argument(
    '-p', '--port',
    default=8000,
    type=int,
    dest='port'
)

if __name__ == '__main__':
    args = parser.parse_args()
    logger.init()
    server = TossServer(args.address, args.port)
    server.run()
