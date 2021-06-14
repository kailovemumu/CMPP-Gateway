package dorox.app.count;

import java.util.Scanner;

public enum CountTypeEnum {

    serverRequest(1), stat(2), success(3);

    private int type;

    CountTypeEnum(int type){
        this.type=type;
    }

    public int getType() {
        return type;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        while(sc.hasNext()){
            int a = Integer.valueOf(sc.next());
            int b = Integer.valueOf(sc.next());
            System.out.println(a+b);
        }
    }
}
