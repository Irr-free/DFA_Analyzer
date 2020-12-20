package lab1;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

public class Lex {
    private Stack<StringTokenizer> rawInputLines;
    private StringTokenizer tokenedInputLines[];
    private Object processedInput[];
    char temp[];
    int size = 0;
    ArrayList<Token> tagList = new ArrayList<>();
    //读取标识
    boolean charread = false;
    boolean strread = false;
    String strstore = new String();
    //关键字
    private static HashMap<String, String> KeyWord = new HashMap<>();

    static {
        KeyWord.put("IDENFR", "IDENFR");
        KeyWord.put("INTCON", "INTCON");
        KeyWord.put("CHARCON", "CHARCON");
        KeyWord.put("STRCON", "STRCON");
        KeyWord.put("error", "ERROR");
        KeyWord.put("const", "CONSTTK");
        KeyWord.put("int", "INTTK");
        KeyWord.put("char", "CHARTK");
        KeyWord.put("void", "VOIDTK");
        KeyWord.put("main", "MAINTK");
        KeyWord.put("if", "IFTK");
        KeyWord.put("else", "ELSETK");
        KeyWord.put("do", "DOTK");
        KeyWord.put("while", "WHILETK");
        KeyWord.put("for", "FORTK");
        KeyWord.put("scanf", "SCANFTK");
        KeyWord.put("printf", "PRINTFTK");
        KeyWord.put("return", "RETURNTK");
    }

    public Lex(String filepath) {
        File file = new File(filepath);
        try {
            Scanner in = new Scanner(file);
            rawInputLines = new Stack<StringTokenizer>();
            while (in.hasNext()) {
                String nextLine = in.nextLine();
                rawInputLines.push(new StringTokenizer(nextLine));
            }
            tokenedInputLines = new StringTokenizer[rawInputLines.size()];
            processedInput = new Object[tokenedInputLines.length];
            for (int i = rawInputLines.size() - 1; i >= 0; i--) {
                StringTokenizer st = (StringTokenizer) rawInputLines.pop();
                tokenedInputLines[i] = st;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("文件打开出错:" + e.toString());
        }
    }

    public void analyse() {
        for (int i = 0; i < tokenedInputLines.length; i++) {
            Stack tempInputLineValues = new Stack();
            while (tokenedInputLines[i].hasMoreTokens()) {
                convertToChars(
                        new StringReader(tokenedInputLines[i].nextToken()));
            }
            Integer inputLineValues[] = new Integer[tempInputLineValues.size()];
            for (int j = tempInputLineValues.size() - 1; j >= 0; j--) {
                inputLineValues[j] = (Integer) tempInputLineValues.pop();
            }
            processedInput[i] = inputLineValues;
        }
    }

    private void convertToChars(StringReader sr) {
        Stack<Integer> s = new Stack();
        int in = -1;
        do {
            try {
                in = sr.read();
            } catch (IOException e) {
                break;
            }
            s.push(in);
        } while (in != -1);
        char characters[] = new char[s.size()];
        for (int i = characters.length - 1; i >= 0; i--) {
            int a = (Integer) s.pop();

            if (i < characters.length - 1) {
                Character b = (char) a;
                characters[i] = b;

            }
        }
//		System.out.println(characters);
//		System.out.println(characters.length);
		if (!strread){//如果不是读取字符串的模式
			dfs(characters, 0);
		}else {//如果是读取字符串的模式
			addstr(characters,0);
		}
    }

//	void print_char(char str[]){
//		String s=new String(str);
//		System.out.println(str);
//    	int len=str.length;
//    	for(int i=0;i<len;i++){
//    		System.out.print(str[i]);
//    	}
//    	System.out.println("--------");
//    }

    private void dfs(char str[], int location) {
        if (location >= str.length - 1) {
            return;    //当已经到了最后一个字符的位置后，已经没有字符串，则跳出
        }
        String token = new String();
        int len = str.length;
        int p = location;
        char ch = str[p];
        //处理字符常量
		if (ch == '\'') {
			tagList.add(new Token(KeyWord.get("CHARCON"), str[++p]));
//			ch = str[++p];
//			while (ch != '\'') {
//				token += ch;
//				ch = str[++p];
//			}
//			if (token.length() == 1) {
//				tagList.add(new Token(KeyWord.get("CHARCON"), token));
//			} else {
//				tagList.add(new Token(KeyWord.get("ERROR"), token));
//			}
			p++;
			dfs(str, ++p);
		}else
        //处理字符串
        if (ch == '\"') {
            ch = str[++p];
            //只要没有读到"或者p没有读到len
            while (ch != '\"' && p < len) {
                token += ch;
                if (p < len - 1) {
                    ch = str[++p];
                } else {
                    break;
                }
            }
            //tagList.add(new Token(KeyWord.get("STRCON"), token));
            //如果是因为读到了"停下来
            if (ch == '\"') {
				tagList.add(new Token(KeyWord.get("STRCON"), token));
            }else {
            	strread = true;
            	token += ' ';
            	strstore += token;
			}
			dfs(str, ++p);
        }else
        //处理整型常量
        if(ch >= '0' && ch <= '9'){
        	while(ch >= '0' && ch <= '9'){
        		token += ch;
        		ch = str[++p];
			}
        	tagList.add(new Token(KeyWord.get("INTCON"),token));
			dfs(str, p);
		}else
        //处理关键字
        if ((ch > 'A' && ch < 'Z') | (ch > 'a' && ch < 'z') | ch == '_') {
            /*标识符或者变量名的情况*/
            while ((ch >= '0' && ch <= '9') | (ch >= 'a' && ch <= 'z') | (ch >= 'A' && ch <= 'Z') | ch == '_') {
                token += ch;
                ch = str[++p];
            }
            boolean tag = false;
            if (KeyWord.containsKey(token)) {
                tagList.add(new Token(KeyWord.get(token), token));
            } else if (token.compareTo("int") == 0) {
                tagList.add(new Token(KeyWord.get("int"), token));
            } else {
                tagList.add(new Token(KeyWord.get("IDENFR"), token));
            }
            dfs(str, p);
        } else {
            token += ch;
            int pp = p + 1;
            switch (ch) {
                case '>':
                    if (str[pp] == '=') {
                        token += str[pp];
                        p = pp + 1;
                        tagList.add(new Token("GEQ", ">="));
                        dfs(str, p);
                    } else {
                        dfs(str, pp);
                        tagList.add(new Token("GRE", ">"));
                    }
                    break;
                case '<':

                    if (str[pp] == '=') {
                        token += str[pp];
                        p = pp + 1;
                        tagList.add(new Token("LEQ", "<="));
                        dfs(str, p);
                    } else {
                        tagList.add(new Token("LSS", "<"));
                        dfs(str, pp);
                    }
                    break;
                case '=':
                    if (str[pp] == '=') {
                        token += str[pp];
                        p = pp + 1;
                        tagList.add(new Token("EQL", "=="));
                        dfs(str, p);
                    } else {
                        tagList.add(new Token("ASSIGN", "="));
                        dfs(str, pp);
                    }
                    break;
//                case '&':
//                    if (str[pp] == '&') {
//                        token += str[pp];
//                        p = pp + 1;
//                        dfs(str, p);
//                    } else {
//                        dfs(str, pp);
//                    }
//                    break;
//                case '|':
//                    if (str[pp] == '|') {
//                        token += str[pp];
//                        p = pp + 1;
//                        dfs(str, p);
//                    } else {
//                        dfs(str, pp);
//                    }
//                    break;
                case '+':
//                    if (str[pp] == '+') {
//                        token += str[pp];
//                        p = pp + 1;
//                        tagList.add(new Token("PLUS", "+"));
//                        tagList.add(new Token("PLUS", "+"));
//                        dfs(str, p);
//                    } else {
//                        tagList.add(new Token("PLUS", "+"));
//                        dfs(str, pp);
//                    }
					tagList.add(new Token("PLUS", "+"));
                    break;
                case '-':
//                    if (str[pp] == '-') {
//                        token += str[pp];
//                        p = pp + 1;
//                        tagList.add(new Token("MINU", "-"));
//                        tagList.add(new Token("MINU", "-"));
//                        dfs(str, p);
//                    } else {
//                        tagList.add(new Token("MINU", "-"));
//                        dfs(str, ++p);
//                    }
					tagList.add(new Token("MINU", "-"));
                    break;
                case '!':
                    if (str[pp] == '=') {
                        token += str[pp];
                        p = pp + 1;
                        tagList.add(new Token("NEQ", "!="));
                        dfs(str, p);
                    } else {
                        tagList.add(new Token("NOT", "!"));
                        dfs(str, pp);
                    }
                    break;
                case '*':
                    tagList.add(new Token("MULT", "*"));
                    break;
                case '/':
                    tagList.add(new Token("DIV", "/"));
                    break;
                case '(':
                    tagList.add(new Token("LPARENT", "("));
                    break;
                case ')':
                    tagList.add(new Token("RPARENT", ")"));
                    break;
                case '{':
                    tagList.add(new Token("LBRACE", "{"));
                    break;
                case '}':
                    tagList.add(new Token("RBRACE", "}"));
                    break;
                case ';':
                    tagList.add(new Token("SEMICN", ";"));
                    break;
                case ',':
                    tagList.add(new Token("COMMA", ","));
                    break;
                case '[':
                    tagList.add(new Token("LBRACK", "["));
                    break;
                case ']':
                    tagList.add(new Token("RBRACK", "]"));
                    break;
            }
            dfs(str, pp);
        }

    }

    //追加字符串
    public void addstr(char str[], int location) {
    	int p = location;
    	char ch = str[p];
    	int len = str.length;
    	while(ch != '\"' && p<len){
			strstore += ch;
			if (p < len - 1) {
				ch = str[++p];
			} else {
				break;
			}
		}
		if (ch == '\"') {
			tagList.add(new Token(KeyWord.get("STRCON"), strstore));
			strread = false;
		}else {
			strstore += ' ';
		}
		dfs(str, ++p);
    }

    public void output() {
        try {
            File file = new File("result.txt");
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            for (int i = 0; i < tagList.size(); i++) {
                if (tagList.get(i).value != null) {
                    System.out.println(tagList.get(i).type + " " + tagList.get(i).value);
                    bw.write(tagList.get(i).type + " " + tagList.get(i).value + "\r\n");
                } else {
                    System.out.println(tagList.get(i).type + " ");
                    bw.write(tagList.get(i).type + " " + "\r\n");
                }
            }
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
