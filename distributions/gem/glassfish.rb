require 'glassfish-10.0-SNAPSHOT.jar'

module GlassFish
  import com.sun.enterprise.glassfish.bootstrap.ASMain
  def startup(args)
    ASMain.main(args.to_java(:string))
  end

  module_function :startup
end
