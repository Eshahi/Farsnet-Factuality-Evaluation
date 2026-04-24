/**
 * FarsNetFactExtractor.java
 *
 * Extracts all synset-to-synset relations from the FarsNet 3.0 SQLite DB
 * (Data/farsnet3.0.db3) and writes them to farsnet_facts.tsv.
 *
 * ─── Project layout (root) ─────────────────────────────────────────────
 *   lib/  farsnet.api-3.0.0.jar
 *   Data/ farsnet3.0.db3
 *   src/  FarsNetFactExtractor.java
 *
 * ─── Compile ───────────────────────────────────────────────────────────
 *   Windows
 *     rmdir /s /q out        & mkdir out
 *     javac -cp "lib\\farsnet.api-3.0.0.jar;." -d out src\\FarsNetFactExtractor.java
 *
 *   Linux/macOS
 *     rm -rf out && mkdir out
 *     javac -cp "lib/farsnet.api-3.0.0.jar:." -d out src/FarsNetFactExtractor.java
 *
 * ─── Run ───────────────────────────────────────────────────────────────
 *   Windows : java -cp "out;lib\\farsnet.api-3.0.0.jar" FarsNetFactExtractor
 *   Linux   : java -cp "out:lib/farsnet.api-3.0.0.jar" FarsNetFactExtractor
 */

import farsnet.schema.Synset;
import farsnet.schema.SynsetRelation;
import farsnet.schema.Sense;
import farsnet.schema.Word;
import farsnet.service.SynsetService;
import farsnet.service.Start;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class FarsNetFactExtractor {

    public static void main(String[] args) {
        System.out.println("Initializing FarsNet API...");

        /* -----------------------------------------------------------------
           1) Build a JDBC URL that points at the *absolute path* of the DB
           ----------------------------------------------------------------- */
        String dbPath = new File("Data/farsnet3.0.db3").getAbsolutePath();
        String jdbcUrl = "jdbc:sqlite:" + dbPath;
        System.out.println("[DEBUG] I will open -> " + jdbcUrl);

        try {
            // FarsNet 3 API expects the JDBC URL in its args
            Start.main(new String[]{ jdbcUrl });
        } catch (Exception e) {
            System.err.println("Error bootstrapping FarsNet DB: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        /* -----------------------------------------------------------------
           2) Create service and prepare TSV output
           ----------------------------------------------------------------- */
        SynsetService service = new SynsetService();
        String outputFile = "farsnet_facts.tsv";
        int factCount     = 0;

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            writer.println("SynsetID\tSynsetWords\tRelationType\tTargetSynsetID");

            List<Synset> synsets = service.getAllSynsets();
            if (synsets == null || synsets.isEmpty()) {
                System.err.println("No synsets found in FarsNet!");
                System.exit(1);
            }
            System.out.printf("Total synsets found: %d%n", synsets.size());

            /* -------------------------------------------------------------
               3) Iterate over synsets and dump relations
               ------------------------------------------------------------- */
            for (Synset syn : synsets) {
                String synsetId = String.valueOf(syn.getId());

                // Collect unique lemmas for this synset
                Set<String> words = new LinkedHashSet<>();
                List<Sense> senses = syn.getSenses();
                if (senses != null) {
                    for (Sense s : senses) {
                        Word w = s.getWord();
                        if (w != null && w.getDefaultValue() != null)
                            words.add(w.getDefaultValue());
                    }
                }
                if (words.isEmpty()) continue;
                String wordsJoined = String.join("|", words);

                // Synset-to-synset links
                List<SynsetRelation> rels = syn.getSynsetRelations();
                if (rels == null || rels.isEmpty()) continue;

                for (SynsetRelation r : rels) {
                    writer.printf("%s\t%s\t%s\t%s%n",
                            synsetId,
                            wordsJoined,
                            r.getType(),
                            String.valueOf(r.getSynsetId2()));
                    factCount++;
                }
            }
        } catch (IOException ioe) {
            System.err.println("IO error: " + ioe.getMessage());
            ioe.printStackTrace();
            System.exit(1);
        }

        System.out.printf("Extraction complete! %d facts written to '%s'.%n",
                factCount, outputFile);
    }
}
