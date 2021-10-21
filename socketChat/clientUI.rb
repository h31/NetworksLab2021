require "socket"
require_relative "client2.rb"
require_relative "msconst/MessageClass.rb"

class Client

  def initialize(ip, port)
    STDOUT.print("Enter username(<20): ")
    @username = STDIN.gets.chomp()
    @server = TCPClient.new(ip, port, @username)
    @server.transmit("", @username, "")
    inbox
    outbox
    @request.join
    @response.join
  end

  def inbox
    @response = Thread.new do
      loop {
        msg = @server.receive
        name = msg.header.username.pack("C*")
        hours = msg.header.hours[0]
        minutes = msg.header.minutes[0]
        text = msg.text.messageStr
        str = sprintf("%s [%02d:%02d]: %s", name, hours, minutes, text)
        if msg.attachment.fileFlag == true
        	File.open(msg.attachment.name+"1"+msg.attachment.extension, "w") do |output|
						output.print msg.attachment.file
        		STDOUT.puts("*#{msg.attachment.name}#{msg.attachment.extension} attached*")
        	end
        end
        STDOUT.puts(str)
      }
    end
  end

  def outbox
    @request = Thread.new do
      loop {
        str = STDIN.gets.chomp
        str = str.split("|||")
        msg = str[0]
        path = str[1]
        if path.nil?
        	path = ""
        end		
        @server.transmit(msg, @username, path)
      }
    end
  end

end    #class end

Client.new("192.168.1.103", "2000")
