require_relative "MessageTextClass.rb"
require_relative 'MessageHeaderClass.rb'
require_relative "MessageAttachmentClass.rb"

class Message
    
  attr_accessor :header
  attr_accessor :text
  attr_accessor :attachment

  def gen(msg, username, path)
    @header = MessageHeader.new().gen(msg, 0, username)
    @text = MessageText.new().gen(msg)
    @attachment = MessageAttachment.new().gen(path)
    return self 
  end
  
  def message
    return @header.hpack + @text.messageStr + @attachment.hpack
  end
  
  def disassemble(str)
    @header = MessageHeader.new().hunpack(str[0..31])
    text = str[header.dataAddres.assemble..header.dataAddres.assemble + header.textSize.assemble - 1]
    @text = MessageText.new().gen(text)
    @attachment = MessageAttachment.new().hunpack(str[header.dataAddres.assemble + header.textSize.assemble..str.length])
    return self
  end
  
  
  def servicePrint
    puts("-----------------------------------")
    puts("Header: ")
    header.servicePrint
    puts("-----------------------------------")
    puts("Text: ")
    text.servicePrintShort
    puts("-----------------------------------")
    puts("Attachment: ")
    attachment.servicePrint
    puts("-----------------------------------")
  end

end
