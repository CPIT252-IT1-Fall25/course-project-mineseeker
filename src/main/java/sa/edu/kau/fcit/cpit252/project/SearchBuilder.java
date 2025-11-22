package sa.edu.kau.fcit.cpit252.project;

import java.util.ArrayList;
import java.util.List;

public final class SearchBuilder {
    private int defaultRadius;
    private final List<SearchTask> tasks = new ArrayList<>();



    public SearchBuilder setDefaultRadius(int r) {
        this.defaultRadius = r;
        return this;
    }


    // this is the logic of separating each prameter in the command and setting the default raduis if not provided
    public SearchBuilder parse(String raw) {
        String[] tokens = raw.split("\\s+");
        for (int i = 0; i < tokens.length; i++) {
            if (i + 1 >= tokens.length) break;

            String name = tokens[i];

            int count;
            try {
                count = Integer.parseInt(tokens[i + 1]);
            } catch (NumberFormatException ex) {
                continue;
            }

            i += 2;

            int radius = defaultRadius;

            if (i < tokens.length) {
                String t = tokens[i];
                if (t.equals("~")) {
                    i++;
                } else if (t.matches("\\d+")) {
                    radius = Integer.parseInt(t);
                    i++;
                }
            }

            tasks.add(new SearchTask(name, count, radius));
        }

        return this;
    }

    public SearchRequest build() {
        return new SearchRequest(List.copyOf(tasks));
    }
}

    //this is the list were all requests are saved as tasks
    record SearchRequest(List<SearchTask> tasks) {}
    record SearchTask(String name, int count, int radius) {}




