import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import resources.ProductResource;

public class MainApplication extends Application<ApplicationConfiguration>{
    public static void main(String[] args) throws Exception{
        new MainApplication().run(args);
    }

    @Override
    public void run(ApplicationConfiguration applicationConfiguration, Environment environment) throws Exception {
        environment.jersey().register(new ProductResource());
        System.out.println("Starting server on port ");

    }
}
