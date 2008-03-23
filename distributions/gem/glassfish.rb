require 'glassfish-10.0-SNAPSHOT.jar'

module GlassFish
  import com.sun.enterprise.glassfish.bootstrap.Main
  def startup(args)
    Main.main(args.to_java(:string))
  end

  module_function :startup
end
