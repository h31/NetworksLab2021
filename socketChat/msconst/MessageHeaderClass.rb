
class MessageHeader             #TODO file attachment
  
  attr_accessor :hours
  attr_accessor :minutes
  attr_accessor :username
  attr_accessor :textSize
  attr_accessor :fileSize
  attr_accessor :dataAddres
  
  
  def initialize
  	@dataAddres = Array.new(2, 0)
  	@dataAddres = [0, 32]
    @hours = Array.new(1, 0)
    @minutes = Array.new(1, 0)
    @username = Array.new(20, 0)
    @textSize = Array.new(3, 0)
    @fileSize = Array.new(5, 0)
  end
  
  def gen(msg, fileSize, username)
    textSizeTmp = msg.unpack("C*").size
    if fileSize <= 1099511627775
      @fileSize = splitBytes(fileSize).rpad(5, 0)
    else
      raise ArgumentError, 'Too big file...('
    end
    if username.length <= 20
      @username = username.unpack("C*").rpad(20, 0)
    else
      raise ArgumentError, 'Too much letters in username...('
    end
    if textSizeTmp <= 16777215
      @textSize = splitBytes(textSizeTmp).rpad(3, 0)
    else
      raise ArgumentError, 'Too much letters in message...('
    end
    return self
  end
  
  def hpack
    str = @dataAddres+@hours+@minutes+@username+@textSize+@fileSize
    str = str.pack("C*")
    return str
  end
  
  def hunpack(str)
    @hours = str[2].unpack("C*")
    @minutes = str[3].unpack("C*")
    @username = str[4..23].unpack("C*").rpad(20, 0)
    @textSize = str[24..26].unpack("C*").rpad(3, 0)
    @fileSize = str[27..31].unpack("C*").rpad(5, 0)
    @dataAddres = str[0..1].unpack("C*").rpad(2, 0)
    return self
  end
  
  def splitBytes(int)
    bytesArray = []
    mask = 0b11111111
    num = int
      while (num > 0)
        byte = num & mask
        bytesArray.unshift(byte)
        num = num >> (8)
      end
    return bytesArray
  end
  
  def assemble(array)
    str = ""
    for num in array
        str = str + "%08b" % num
    end
    return str.to_i(2)
  end
  
  def servicePrint
    puts("Data addres: " + assemble(@dataAddres).to_s)
    puts("Hours: " + @hours.to_s)
    puts("Minutes: " + @minutes.to_s)
    puts("username: " + @username.pack("C*"))
    puts("Message size: " + assemble(@textSize).to_s)
    puts("File size: " + assemble(@fileSize).to_s)
  end
  

end # Exp end

class Array
  
  def rpad(n, x); insert(0, *Array.new([0, n-length].max, x)) end
  
  def lpad(n, x); fill(x, length...n) end
  
  def assemble
    str = ""
    for num in self
        str = str + "%08b" % num
    end
    return str.to_i(2)
  end
end
