#!/usr/bin/ruby
#
# Author: Dies Koper (dkoper@dev.java.net)
# This script reads a list of message keys from an input file and
# checks all Java source files for the usage of these keys.
# Message logged for debugging (level FINE, etc.) are omitted from the results.
require 'find'

keys_found = []
# load message keys (or "msg.key=msg text" pairs)
keys = IO.readlines("msgs-with-no-ids.txt")
keys.collect! { |item|
  # only interested in message keys (so remove msg text)
  if item =~ /^([\w._\-]+\s*[^\\])[=:].*/
    $1
  else
    item
  end
}

# return regex pattern from given array in .*"(a|b|c)".* format, i.e.
# a, b or c is enclosed in double quotes
def build_regex(keys)
  #puts keys
  regex=".*\"("
  keys.each { |key|
    regex += key.to_s + '|'
  }
  regex = regex.chomp!('|') + ')".*'
  #puts regex
end

#puts keys
# build regex pattern from array elements
regex = build_regex(keys)
#puts regex

keys_total = keys.size
matches = 0
no_id = 0
prev_line = ""

puts "GlassFish Home Is: "+ARGV[0] 

# traverse all files under the following root dir
Find.find(ARGV[0] ) do |f|

  case File.dirname(f)
    # skip directories that store copies of property files
  when /.*\/.svn/ then Find.prune # skip .svn dirs
  when /.*\/target/ then Find.prune # skip build dirs
  when /.*\/tests/ then Find.prune # skip test dirs
  end

  case File.extname(f)
  when '.java' then
    IO.foreach(f) {|line|
      # if line contains a message key
      unless (line.match(regex).nil?)
        key = $1
        keys_found << key

        case line
          # if a line starts with a double quote or opening bracket '(' it is
          # probably a continuance of the previous line
        when /^\s*["\(].*/
          line = prev_line.chomp + line.lstrip
          #puts "double line: #{line}"
        end

        case line
          # do not report if message logged as debug message
        when /.*log\(Level\.(FINE|FINER|FINEST|CONFIG).*/ then
          #          puts "debug msg found: #{key} in #{f.gsub(File::SEPARATOR,
          #          File::ALT_SEPARATOR || File::SEPARATOR)}\n#{line}"
        when /.*\.(fine|finer|finest|config)\(.*/ then
          #          puts "debug msg found: #{key} in #{f.gsub(File::SEPARATOR,
          #          File::ALT_SEPARATOR || File::SEPARATOR)}\n#{line}"
        else
          # Message key found. (Could be in code that is commented out,
          # not used, logged as info/warn/severe message, etc.)
          puts "- Found key '#{key}' used in #{f.gsub(File::SEPARATOR,
          File::ALT_SEPARATOR || File::SEPARATOR)}\n#{line}"
          no_id += 1
        end
      end
      prev_line = line
    }
  end

end

keys_not_found = keys - keys_found
puts "#{no_id} messages may need an Id."
puts "#{keys_not_found.size} of provided #{keys_total} message keys were not found:\n" +
  keys_not_found.inspect
