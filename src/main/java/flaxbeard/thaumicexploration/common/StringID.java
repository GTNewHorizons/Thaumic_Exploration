package flaxbeard.thaumicexploration.common;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Katrina on 07/06/2015.
 */
public class StringID {

    public static String getName() {
        String Output = "";
        try {

            List<String> adjectivesFile = getList("assets/thaumicexploration/misc/adjectives");
            List<String> animalsFile = getList("assets/thaumicexploration/misc/animals");

            Random rand = new Random();

            int num = rand.nextInt(adjectivesFile.size() - 1);
            Output = Output
                    + adjectivesFile.get(num).substring(0, 1).toUpperCase()
                    + adjectivesFile.get(num).substring(1);

            num = rand.nextInt(adjectivesFile.size() - 1);
            Output = Output
                    + adjectivesFile.get(num).substring(0, 1).toUpperCase()
                    + adjectivesFile.get(num).substring(1);

            num = rand.nextInt(animalsFile.size() - 1);
            Output = Output
                    + animalsFile.get(num).substring(0, 1).toUpperCase()
                    + animalsFile.get(num).substring(1);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return Output;
    }

    public static List<String> getList(String name) throws IOException {
        InputStream strm = StringID.class.getClassLoader().getResourceAsStream(name);
        List<String> strings = new ArrayList<String>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(strm));
        String StrArr = null;
        String str = reader.readLine();
        while (str != null) {
            strings.add(str);
            str = reader.readLine();
        }
        return strings;
    }
}
