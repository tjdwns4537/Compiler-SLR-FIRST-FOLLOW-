import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class ProjectMain {
    static ArrayList<Character> cArr = new ArrayList<>();
    static ArrayList<Character> StartNonTerminal = new ArrayList<>();
    static ArrayList<Character> FIRSTARR = new ArrayList<>();
    static ArrayList<Character> FOLLOWARR = new ArrayList<>();
    static char[][] FOLLOWSTORE = new char[10][10];
    static char[][] SLRArr = new char[30][20];
    static char[][] CLOSURE = new char[30][20];
    static int startcntArr[] = new int[10];
    static int endcntArr[] = new int[10];
    static int flag=0;
    static int startcnt=0;
    static int endcnt=0;
    static int FOLLOW_Terminal_cnt =0;
    static int FOLLOW_NonTerminal_cnt =0;
    static int State=0;
    static HashMap<String, String> StateOrder = new HashMap<>();
    static HashMap<String, String> ReductStore = new HashMap<>();
    static int stateCnt = 1;
    static char inpTemp=' ';
    static String[] PlusG = new String[15];


    /*
                 SLRArr 중에 점 뒤에 문자열이 있는 증가문법 기호들을 CLOSURE로 넣어준다.
                 State 는 현재 상태를 나타냄
                 CLOSURE 를 재귀시켜야하며,
                 NewArr 은 그저 CLOSURE를 저장하는 용도이며, 상태의 갯수도 같이 출력할 수 있도록 해준다.
    */

    public static void main(String[] args) {
        String path = "/Users/parksungjun/Desktop/2022-01/컴파일러 수업/컴파일러.txt";
        read_File(path);

        //SLR 구문분석
        SLR();

        // FIRST 구하기
        char tempFIRST = ' ';
        for (int i = 0; i < StartNonTerminal.size(); i++) {
            try{
                if(!StartNonTerminal.get(i).equals(tempFIRST)) {
                    System.out.print(StartNonTerminal.get(i) + "의 FIRST : { ");
                }
                // 화살표 왼쪽 논터미널들의 FIRST를 다 넣어준다.
                FIRST_func(i,StartNonTerminal.get(i));

                for (int j = 0; j < FIRSTARR.size(); j++) {
                    if(FIRSTARR.get(j) == 'i'){
                        System.out.print("id ");
                    }
                    else System.out.print(FIRSTARR.get(j)+" ");
                }
                if(StartNonTerminal.get(i).equals(tempFIRST)) {
                    System.out.println(" }");
                }
                tempFIRST = StartNonTerminal.get(i);
                FIRSTARR.clear();
            }catch (IndexOutOfBoundsException e){

            }
        }

        // FOLLOW 구하기
        char tempFOLLOW = ' ';
        for (int i = 0; i < StartNonTerminal.size(); i++) {
            try{
                if(!StartNonTerminal.get(i).equals(tempFOLLOW)){
                    System.out.print(StartNonTerminal.get(i) + "의 FOLLOW : {");
                    if(i==0) FOLLOWARR.add('$');
                    FOLLOW_func(StartNonTerminal.get(i));
                    tempFOLLOW = StartNonTerminal.get(i);
                    System.out.println("}");
                }
                for (int k = 0; k < FOLLOWSTORE.length; k++) {
                    for (int m = 0; m < FOLLOWSTORE[k].length; m++) {
                        FOLLOWSTORE[k][m] = 0;
                    }
                }
                //FOLLOWSTORE 2차원 배열에 FOLLOW값을 저장
                for (int k = 0; k < StartNonTerminal.size(); k++) {
                    FOLLOWSTORE[FOLLOW_Terminal_cnt][0] = StartNonTerminal.get(k);
                    for (int m = 0; m < FOLLOWARR.size(); m++) {
                        FOLLOWSTORE[FOLLOW_Terminal_cnt][FOLLOW_NonTerminal_cnt] = FOLLOWARR.get(m);
                        if(m != (FOLLOWARR.size()-1)) FOLLOW_NonTerminal_cnt++;
                    }
                    if(k != (StartNonTerminal.size()-1)) FOLLOW_Terminal_cnt++;
                }
                FOLLOWARR.clear();


            }catch (IndexOutOfBoundsException e){

            }
        }
    }

    public static void SLR() {
        //초기화
        for (int i = 0; i < SLRArr.length; i++) {
            for (int j = 0; j < SLRArr[i].length; j++) {
                SLRArr[i][j] = ' ';
            }
        }

        for (int i = 0; i < CLOSURE.length; i++) {
            for (int j = 0; j < CLOSURE[i].length; j++) {
                CLOSURE[i][j] = ' ';
            }
        }

        for (int i = 0; i < PlusG.length; i++) {
            PlusG[i] = "";
        }

        //증가 문법을 만들기 위해 배열 안에 다 넣어준다.
        SLRArr[0][0] = 'S';
        SLRArr[0][1] = '=';
        SLRArr[0][2] = '=';
        SLRArr[0][3] = '>';
        SLRArr[0][4] = '.';
        SLRArr[0][5] = StartNonTerminal.get(0);
        for (int i = 0; i<StartNonTerminal.size(); i++) {
            SLRArr[i+1][0] = StartNonTerminal.get(i);
            SLRArr[i+1][1] = '=';
            SLRArr[i+1][2] = '=';
            SLRArr[i+1][3] = '>';
            SLRArr[i+1][4] = '.';
            int cArrNm = startcntArr[i]; // end 배열의 i, start배열의 i를 빼주면 해당 범위가 나온다. 그 시작점을 cArrNm에 할당해줌
            for (int j = 0; j <= endcntArr[i]-startcntArr[i]; j++) {
                if(cArr.get(cArrNm) != '\n'){
                    SLRArr[i+1][j+5] = cArr.get(cArrNm);
                    cArrNm++;
                }
            }
        }

        // 증가문법 저장 완료
        System.out.println("[증가문법]");
        for (int i = 0; i < SLRArr.length; i++) {
            if(SLRArr[i][0] == ' ') break;
            for (int j = 0; j < SLRArr[i].length; j++) {
                System.out.print(SLRArr[i][j]);
            }
            System.out.println();
        }
        System.out.println();

        // 감축을 위해 SLRArr배열고 똑같은 배열을 하나 만들어줌
        String PlusGtemp = "";
        char[][] tempArr = new char[30][30];
        for(int i=0; i < SLRArr.length; i++){
            for(int j = 0; j < SLRArr[i].length; j++){
                tempArr[i][j] = SLRArr[i][j];
            }
        }
        // 증가문법에서 점 기호 뺀것을 복사
        for (int i = 0; i < tempArr.length; i++) {
            PlusGtemp = "";
            if(tempArr[i][0] == ' ') break;
            for (int j = 0; j < tempArr[i].length; j++) {
                if (tempArr[i][j] == '.' && tempArr[i][j+1] == ' ') {
                    break;
                }
                if (tempArr[i][j] == '.') {
                    char temp = tempArr[i][j];
                    tempArr[i][j] = tempArr[i][j+1];
                    tempArr[i][j + 1] = temp;
                }
                PlusGtemp += String.valueOf(tempArr[i][j]);
            }
            PlusG[i] = PlusGtemp;
        }

        // LR0 호출
        LR0();

        List<Map.Entry<String, String>> list_entries = new ArrayList<Map.Entry<String, String>>(StateOrder.entrySet());
        List<Map.Entry<String, String>> list_entries2 = new ArrayList<Map.Entry<String, String>>(ReductStore.entrySet());

        Collections.sort(list_entries, new Comparator<Map.Entry<String, String>>() {
            @Override
            public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        Collections.sort(list_entries2, new Comparator<Map.Entry<String, String>>() {
            @Override
            public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });

        System.out.println();
        System.out.println("------------이동------------");
        for(Map.Entry<String, String> entry : list_entries) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }

        System.out.println();
        System.out.println("----------------감축항목-------------");

        for(Map.Entry<String, String> entry : list_entries2) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
        System.out.println();

        System.out.println("----------------SLR 표-------------");
        char[] Non_AlpArr = new char[20];
        char[] Ter_AlpArr = new char[20];
        int Non_AlpArr_cnt = 0;
        int Ter_AlpArr_cnt = 0;
        for (int i = 0; i < cArr.size(); i++) {
            if('A' <= cArr.get(i) && cArr.get(i) <= 'Z'){
                Non_AlpArr[Non_AlpArr_cnt] = cArr.get(i);
                Non_AlpArr_cnt = Non_AlpArr_cnt + 1;
            }
            if('a' <= cArr.get(i) && cArr.get(i) <= 'z'){
                Ter_AlpArr[Ter_AlpArr_cnt] = cArr.get(i);
                Ter_AlpArr_cnt = Ter_AlpArr_cnt+1;
            }
            if('+' ==cArr.get(i) || '-' ==cArr.get(i) || '/' ==cArr.get(i) || '&' ==cArr.get(i) ||
            '#' ==cArr.get(i) || '!' ==cArr.get(i) || '%' ==cArr.get(i) || '$' ==cArr.get(i) ||
            ')' ==cArr.get(i) || '(' ==cArr.get(i) || '*' ==cArr.get(i)){
                Ter_AlpArr[Ter_AlpArr_cnt++] = cArr.get(i);
                Ter_AlpArr_cnt = Ter_AlpArr_cnt+1;
            }
            if('i' ==cArr.get(i) && 'd' ==cArr.get(i)){
                Ter_AlpArr[Ter_AlpArr_cnt] = cArr.get(i);
                Ter_AlpArr[Ter_AlpArr_cnt+1] = cArr.get(i);
                Ter_AlpArr_cnt = Ter_AlpArr_cnt+2;
            }

        }

    }
    static int Bignum = 0;

    public static void LR0() {
        /*
        SLRArr 배열 : 증가문법의 규칙이 있기때문에 원본을 건드리지 않음
        CLOSURE : SLRArr을 그대로 복사해 수정해줌
         */
        for (int i = 0; i < SLRArr.length; i++) {
            for (int j = 0; j < SLRArr[i].length; j++) {
                CLOSURE[i][j] = SLRArr[i][j];
            }
        }
        // 상태 이동에 대한 반복문
        while(true){
            int stateFlag = 1; // 1 이 0 이되면 . 뒤에 문자열이 없는 것이다.
            int cc = 0;
            for (int i = 0; i < CLOSURE[cc].length; i++) {
                for (int j = 0; j < CLOSURE.length; j++) {
                    if( (CLOSURE[j][i] == '.') && (CLOSURE[j][i+1] != ' ') ){
                        stateFlag = 0;
                        LR0_GOTO(CLOSURE[j][i+1],j);
                    }
                }
                cc++;
            }
            if(stateFlag == 0) break;
            System.out.println();
        }

        for (int i = 0; i < CLOSURE.length; i++) {
            for (int j = 0; j < CLOSURE[i].length; j++) {
                if( (CLOSURE[i][j] == '.') && (CLOSURE[i][j+1] == ' ') ){
                    LR0_REDUCE(i);
                }
            }
        }
    }

    // GOTO 함수
    public static void LR0_GOTO(char ch, int a) {
        String str="";
        // ( I0,E ) 에 해당하는곳 출력 부분
        if(ch == 'i'){
            str = String.valueOf(ch)+"d";
            System.out.print("(State" + "," + str + ") : ");
        }
        else{
            System.out.print("(State" + "," + ch + ") : ");
        }
        for (int i = 0; i < CLOSURE[a].length; i++) {
            if(CLOSURE[a][i] == 'i') CLOSURE[a][i+1] = 'd';
            CLOSURE[a][i] = CLOSURE[a][i];
        }

        // CLOSURE 출력 부분
        System.out.print("CLOSURE:" );
        for (int i = 0; i < CLOSURE[a].length; i++) {
            System.out.print(CLOSURE[a][i]);
        }

        System.out.print(" = ");
        System.out.print("{ " );
        // . 위치 바꾸는 부분
        for (int i = 0; i < CLOSURE[a].length; i++) {
            if(CLOSURE[a][i] == '.' && CLOSURE[a][i+1] != 'i'){
                LR0_Swap(a,i,i+1);
                break;
            }
            // id 가 들어왔을 때의 조건문
            if(CLOSURE[a][i] == '.' && CLOSURE[a][i+1] == 'i'){
                LR0_Swap(a,i, i+3);
                break;
            }
        }

        // . 위치 바꾼 후 출력
        for (int i = 0; i < CLOSURE[a].length; i++) {
            System.out.print(CLOSURE[a][i]);
        }

        System.out.print(" } ");

        // . 위치 바꾼 클로져를 저장해준다.
        stateStore(a,ch);
        System.out.println();
    }

    // 상태 저장 함수
    public static void stateStore(int a,char ch) {
        String Cando = " ";
        String I = "";

        if(stateCnt >= 10){
            I = "I" + "_" + stateCnt; // 상태를 스트링으로 저장
        }else{
            I = "I" + stateCnt; // 상태를 스트링으로 저장
        }

        // 상태를 저장
        if(inpTemp == ch){
            // 이전 키 값을 찾기 위한 반복문
            for (Map.Entry<String, String> entry : StateOrder.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (value.equals(stateCnt)) {
                    Cando += value;
                    Cando += ", ";
                }
            }
            for (int i = 0; i < CLOSURE[a].length; i++) {
                Cando += CLOSURE[a][i]; // Char로 입력된 텍스트를 스트링을 저장
            }
        }
        else{
            for (int i = 0; i < CLOSURE[a].length; i++) {
                Cando += CLOSURE[a][i]; // Char로 입력된 텍스트를 스트링을 저장
            }
        }
        StateOrder.put(Cando, I); // 각 상태의 숫자와 규칙을 저장함
        if(inpTemp == ch){

        }
        else stateCnt++;
        inpTemp = ch;

    }

    // 자리 바꾸는 함수
    public static void LR0_Swap(int t,int a,int b){
        char temp = CLOSURE[t][a];
        CLOSURE[t][a] = CLOSURE[t][b];
        CLOSURE[t][b] = temp;
    }

    // 감축함수
    public static void LR0_REDUCE(int idx){
        String str = "";
        String reduce = "";

        for (int i = 0; i < CLOSURE[idx].length; i++) {
            if(CLOSURE[idx][i] == ' ' || CLOSURE[idx][i] == '.') break;
            str += CLOSURE[idx][i];
        }

        // id 에 대한 에러를 처리하기 위한 조건문
        if(str.equals("F==>")){
            str = "F==>id";
        }


        for (int i = 0; i < PlusG.length; i++) {
            if(PlusG[i].equals(str)){
                reduce = "R"+i;
                ReductStore.put(str,reduce);
            }
        }


    }

    public static void read_File(String path){
        try {
            FileReader fileReader = new FileReader(path);
            int ch = 0;
            while ((ch = fileReader.read()) != -1) {
                char CharCh = (char)ch;
                cArr.add(CharCh);
            }
            //시작논터미널 확인
            char all[] = new char[100];
            for (int i = 0; i < cArr.size(); i++) {
                try{
                    if(cArr.get(i+1).equals('=') && ('A' <= cArr.get(i) && cArr.get(i) <= 'Z')){
                        StartNonTerminal.add(cArr.get(i));
                        flag = 0;
                    }
                    if(cArr.get(i).equals('>')){
                        startcntArr[startcnt] = i+1;
                        startcnt++;
                    }
                    if(cArr.get(i).equals('\n') && flag == 0){
                        endcntArr[endcnt] = i;
                        endcnt++;
                        flag = 1;
                    }
                }
                catch (IndexOutOfBoundsException e){
                }
            }
            endcntArr[endcnt] = cArr.size()-1;

            System.out.println("start & end cnt");
            for (int i = 0; i < startcnt; i++) {
                System.out.print(startcntArr[i] + " ");
            }
            System.out.println();
            for (int i = 0; i < startcnt; i++) {
                System.out.print(endcntArr[i] + " ");
            }
            System.out.println();
            System.out.println("-----");
        } catch (FileNotFoundException e) {
            System.out.println("파일 디렉터리가 맞지않습니다.");
        } catch (IOException e){
            System.out.println("IOException 발생");
        }
    }

    //FIRST 함수
    public static void FIRST_func(int selectPv,char input_NonTerminal) {
        //selectPv 입력받은 논터미널이 위치하는 인덱스
        //논터미널이라면
        if ('A' <= input_NonTerminal && input_NonTerminal <= 'Z') {
            if ('a' <= cArr.get(startcntArr[selectPv]) && cArr.get(startcntArr[selectPv]) <= 'z') {
                //id 값을 받아들이기 위한 조건
                if (FIRST_func_checked(FIRSTARR, cArr.get(startcntArr[selectPv])) == 1) { // 중복 체크
                    FIRSTARR.add(cArr.get(startcntArr[selectPv]));
                }
            }

            if ('(' == cArr.get(startcntArr[selectPv]) || cArr.get(startcntArr[selectPv]) == ')' || cArr.get(startcntArr[selectPv]) == '+'
                    || cArr.get(startcntArr[selectPv]) == '/' || cArr.get(startcntArr[selectPv]) == '%' || cArr.get(startcntArr[selectPv]) == ','
                    || cArr.get(startcntArr[selectPv]) == '*') {
                if (FIRST_func_checked(FIRSTARR, cArr.get(startcntArr[selectPv])) == 1) {
                    FIRSTARR.add(cArr.get(startcntArr[selectPv]));
                }
            }

            // FIRST로 대문자가 들어올 경우
            if ('A' <= cArr.get(startcntArr[selectPv]) && cArr.get(startcntArr[selectPv]) <= 'Z') {
                if (input_NonTerminal == cArr.get(startcntArr[selectPv])) return;
                else {
                    if (FIRST_func_checked(FIRSTARR, cArr.get(startcntArr[selectPv])) == 1) {
                        //입력된 논터미널의 인덱스를 저장하는 배열
                        int[] temp = new int[10];
                        int cnt = 0;
                        // 입력된 논터미널에 해당하는 인덱스를 저장하기위 한 반복문
                        for (int n = 0; n < StartNonTerminal.size(); n++) {
                            if(StartNonTerminal.get(n) == cArr.get(startcntArr[selectPv])){
                                temp[cnt++] = n;
                            }
                        }
                        // 논터미널일 경우 재귀를 통해 FIRST를 구해준다. 반복문은 F가 2개일경우를 대비해서 작성한것이다.
                        for (int b = 0; b < cnt; b++) {
                            FIRST_func(temp[b],cArr.get(startcntArr[selectPv]));
                        }
                    }
                }
            }
        }
    }

    //중복체크함수
    public static int FIRST_func_checked(ArrayList<Character> arr, char ch ) {
        for (char i : arr) {
            if(i == ch) return 0;
        }
        return 1;
    }

    // FOLLOW 함수
    public static void FOLLOW_func(char input_NonTerminal) {
        if('A'<= input_NonTerminal && input_NonTerminal <= 'Z'){
            for (int i = 0; i<StartNonTerminal.size(); i++) {
                for (int j = startcntArr[i]; j < endcntArr[i]; j++) {
                    if (cArr.get(j).equals(input_NonTerminal)) {
                        if('a'<= cArr.get(j+1) && cArr.get(j+1) <= 'z'){
                            if(FOLLOW_func_checked(FOLLOWARR,cArr.get(j+1))==1){
                                FOLLOWARR.add(cArr.get(j+1));
                            }
                        }
                        if('(' == cArr.get(j+1) || cArr.get(j+1) == ')'||cArr.get(j+1) == '+'
                                || cArr.get(j+1) == '/'|| cArr.get(j+1) == '%'|| cArr.get(j+1) == ','
                                || cArr.get(j+1) == '*')
                        {
                            if(FOLLOW_func_checked(FOLLOWARR,cArr.get(j+1)) ==1){
                                FOLLOWARR.add(cArr.get(j+1));
                            }
                        }
                        if('A' <= cArr.get(j+1) && cArr.get(j+1) <= 'Z'){
                            if(input_NonTerminal == cArr.get(j+1)) break;
                            else {
                                FOLLOW_func(cArr.get(j+1));
                            }
                        }
                        if('\n' == cArr.get(j+1)){
                            int cnt = 0;
                            for(int u=0; u<endcntArr.length; u++){
                                if(endcntArr[u] == j){
                                    cnt = u;
                                }
                            }
                            for (int m = 0; m < FOLLOWSTORE[cnt].length; m++) {
                                if(FOLLOWSTORE[cnt][m] != 0){
                                    if(FOLLOW_func_checked(FOLLOWARR,FOLLOWSTORE[cnt][m]) == 1){
                                        FOLLOWARR.add(FOLLOWSTORE[cnt][m]);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            for(int i=0; i<FOLLOWARR.size(); i++){
                if(i < FOLLOWARR.size()-1) System.out.print(FOLLOWARR.get(i)+",");
                else System.out.print(FOLLOWARR.get(i));
            }
        }
    }

    public static int FOLLOW_func_checked(ArrayList<Character> arr, char ch ) {
        for (char i : arr) {
            if(i == ch) return 0;
        }
        return 1;
    }
}
