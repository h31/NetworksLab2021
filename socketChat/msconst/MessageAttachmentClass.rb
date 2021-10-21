class MessageAttachment

	attr_accessor :name
	attr_accessor :extension
	attr_accessor :extension_length
	attr_accessor :file
	attr_accessor :name_length
	attr_accessor :fileFlag

	def gen(path)
		if File.exist?(path)
			puts "exists"
			@extension = File.extname(path)
			@extension_length = extension.length
			@name = File.basename(path, extension)
			@name_length = name.length
			@file = File.binread(path).unpack("C*").pack("C*")
			@fileFlag = true
			return self
		else
			@fileFlag = false
			return self
		end
	end
	
	def hpack()
		if fileFlag == true
			str = [@name_length].pack('C*')+[@extension_length].pack('C*')+@name+@extension+@file
		else
			str = ""
		end
		return str
	end
	
	def hunpack(str)
		if (str.nil? || str == "")
			@fileFlag = false
			return self
		else
			@fileFlag = true
			@name_length = str[0].unpack("C*")[0]
			@extension_length = str[1].unpack("C*")[0]
			@name = str[2..(2+name_length-1)]
			@extension = str[(2+name_length)..(2+name_length+extension_length-1)]
			@file = str[(2+name_length+extension_length)..str.length]
	  	return self
		end
	end
	
	  def servicePrint
    puts("name: " + @name.to_s)
    puts("Extension: " + @extension.to_s)
    puts("Name length: " + @name_length.to_s)
    puts("Extension length: " + @extension_length.to_s)
    puts("File flag: " + @fileFlag.to_s)
  end

end
