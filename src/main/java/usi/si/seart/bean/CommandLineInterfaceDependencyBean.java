package usi.si.seart.bean;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContextException;
import org.springframework.stereotype.Component;
import usi.si.seart.io.ExternalProcess;

import java.util.List;
import java.util.stream.Collectors;

@Component("CommandLineInterfaceDependencyBean")
public class CommandLineInterfaceDependencyBean implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        String check = List.of("cloc", "curl", "git").stream()
                .map(name -> name + " --version")
                .collect(Collectors.joining(" && ", "(", ") > /dev/null"));
        new ExternalProcess("/bin/sh", "-c", check).execute().ifFailedThrow(
                () -> new ApplicationContextException("The dependency requirements were not met!")
        );
    }
}
