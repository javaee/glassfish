#!/usr/bin/ruby
#
# Author: Dies Koper (dkoper@dev.java.net)
# This script reads all LogStrings.properties in the current directory and
# its subdirectories and confirms the messages in them have proper Ids.
# The results (except for messages on the exclude list) are written to a file.
require 'find'

$excl_list = Array.new
# load excluded messages list
# this list includes message keys of messages for which it has been
# confirmed that they should not have a msg id (like debug messages).
IO.foreach("./message-key-excl.txt") { |line|
  $excl_list.push line.chomp if line =~ /^[\w\-].*$*/
}

hits = []
file_count = 0

puts "GlassFish Home Is: "+ARGV[0] 


# traverse all files under the following root dir
Find.find(ARGV[0]) do |f|

  # skip directories that store copies of property files
  case File.dirname(f)
  when /.*\/.svn/ then Find.prune # skip .svn dirs
  when /.*\/target/ then Find.prune # skip build dirs
  when /.*\/tests/ then Find.prune # skip test dirs
  end

  case File.basename(f)
  when 'LogStrings.properties' then
    file_count += 1
    # print filename using OS specific path
    puts "* #{f.gsub(File::SEPARATOR,
    File::ALT_SEPARATOR || File::SEPARATOR)}"

    # open each msg properties file and read line by line
    File.open(f) do |msgfile|
      msgfile.each_line { |line|
        #        puts "line: #{line}"
        if (needs_id(msgfile, line) == true)
          hits << line
          puts "  - no Id: #{line}"
        end
      }
    end
  end

  def needs_id(file, line)
    # concate multi-line message \
    is_multi_line = false
    while (!line.to_s.match(/.+\\\s*$/).nil?)
      next_line = file.gets.lstrip
      line = line.chop + next_line
      is_multi_line = true
    end
    #    puts "\tmulti-line msg: #{line}" if is_multi_line

    id_req = false
    case line

      # id.diag....
    when /^\s*[\w_\-]+\.diag\.(cause|check)\..*/
      #puts "diag msg: #{line}"

      # msg.key = PREFIX123: ... (colon may be escaped - LAUNHCER006)
    when /^\s*[\w._\-]+\s*[^\\][=:]\s*\w+\d+\s*\\?:.*/
      #      puts "msg with key: #{line}"

      # comment saying next message needs no id
      # used in web\war-util\src\main\resources\com\sun\logging\enterprise\system\container\web\LogStrings.properties
    when /^\s*[#!]no ID on .*/
      #skip next message
      line = file.gets
      #      puts "\tskip next msg: #{line}"

      # comment
    when /^\s*[#!].*$/

      # no msg
    when /^\s*$/

      # msg.key = Message with no prefix
    when /^([\w._\-]+\s*[^\\])[=:].*/
      id_req = true unless $excl_list.include?($1)
    else
      puts "  - unknown: #{line}"

    end
    id_req
  end
end

puts "\nFinished."
puts "Searched #{file_count} files and found #{hits.size} messages with no Id (with #{$excl_list.size} messages excluded)"
puts "\nResults have been saved to ./msgs-with-no-ids.txt"

File.open("./msgs-with-no-ids.txt", 'w') { |f|
  hits.each { |item|
    f.write item
  }
}
