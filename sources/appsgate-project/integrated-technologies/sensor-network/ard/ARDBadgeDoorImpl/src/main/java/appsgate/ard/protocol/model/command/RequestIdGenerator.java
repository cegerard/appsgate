package appsgate.ard.protocol.model.command;


public class RequestIdGenerator {

    private static Integer sharedCounter=0;

    private static RequestIdGenerator generator;

    private RequestIdGenerator(){}

    public static RequestIdGenerator getInstance(){

        if(generator==null){
            generator=new RequestIdGenerator();
        }

        return generator;
    }

    public Integer genId(){
        return sharedCounter++;
    }

}
