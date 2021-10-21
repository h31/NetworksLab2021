require "socket"
require_relative "msconst/MessageClass.rb"

class TCPClient

  def initialize(ip, port, username)
    @server = TCPSocket.open(ip, port)
  end

  def receive
    msg = @server.gets.chomp
    msg = Message.new().disassemble(msg)
    #msg.servicePrint
    return msg
    
  end

  def transmit(msg, username, path)
    message = Message.new().gen(msg, username, path)
    #message.servicePrint
    message = message.message
    @server.puts(message)
  end

end    #class end


