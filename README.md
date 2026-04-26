# FarsNet Factuality Evaluation

A research-oriented pipeline for evaluating the factual consistency of Persian FarsNet article statements against evidence retrieved from Persian Wikipedia.

The project separates the workflow into two main stages:

1. **Fact extraction** — a Java utility extracts candidate factual statements from FarsNet article JSON files.
2. **Fact verification** — a Jupyter notebook searches Persian Wikipedia, compares extracted facts with Wikipedia summaries, and produces evaluation reports.

> **Note**
> This project uses heuristic evidence matching against Wikipedia summaries. A verified result should be treated as supporting evidence, not as definitive proof of truth. Likewise, an unverified result may mean that the relevant evidence was not present in the retrieved Wikipedia summary.

## Features

* Extract candidate factual statements from Persian article text.
* Support article text stored in either `content` or `description` fields.
* Filter short, question-like, and noisy sentences.
* Clean extracted facts by removing citations, parenthetical text, and URLs.
* Limit extracted facts per article to keep evaluation manageable.
* Search Persian Wikipedia by exact article title, redirects, and fallback search.
* Compare facts using exact matching, keyword overlap, and fuzzy sentence similarity.
* Generate structured JSON evaluation results.
* Export a CSV summary for analysis and reporting.

## Repository Structure

```text
.
├── FarsNetFactExtractor.java      # Java utility for extracting candidate facts from FarsNet JSON
├── farsnet_factuality.ipynb       # Jupyter notebook for Wikipedia-based factuality evaluation
└── README.md                      # Project documentation
```

## Workflow Overview

```text
FarsNet article JSON
        │
        ▼
FarsNetFactExtractor.java
        │
        ▼
Extracted facts JSON
        │
        ▼
farsnet_factuality.ipynb
        │
        ▼
Wikipedia-backed factuality results
        │
        ▼
JSON report + optional CSV summary
```

## Input Format

The Java extractor expects a JSON array of article objects. Each object should include a `title` field and either a `content` or `description` field.

```json
[
  {
    "title": "عنوان مقاله",
    "content": "متن مقاله شامل چند جمله فارسی ..."
  },
  {
    "title": "عنوان مقاله دوم",
    "description": "متن جایگزین مقاله ..."
  }
]
```

## Fact Extraction Output

The extractor writes a JSON array where each article contains its title and a list of extracted factual statements.

```json
[
  {
    "title": "عنوان مقاله",
    "facts": [
      "جمله factual استخراج شده اول.",
      "جمله factual استخراج شده دوم."
    ]
  }
]
```

## Factuality Evaluation Output

The notebook reads the extracted facts and evaluates each fact against Persian Wikipedia. The generated results include article-level and fact-level information such as:

* article title;
* total number of extracted facts;
* number of verified facts;
* article-level accuracy;
* fact verification status;
* confidence level;
* supporting evidence when available;
* matched Persian Wikipedia page.

The notebook can also export a CSV summary with columns such as:

```text
Article Title, Total Facts, Verified Facts, Accuracy, Fact, Status, Verified, Confidence, Evidence, Wikipedia Page
```

## Requirements

### Java

* JDK 8 or newer
* `org.json` Java library

### Python

* Python 3.8 or newer
* Jupyter Notebook or JupyterLab
* `requests`

The notebook also uses Python standard-library modules such as `json`, `time`, `urllib.parse`, `difflib`, and CSV utilities.

### External Access

The evaluation notebook requires internet access to query Persian Wikipedia APIs.

## Installation

Clone the repository:

```bash
git clone https://github.com/Eshahi/Farsnet-Factuality-Evaluation.git
cd Farsnet-Factuality-Evaluation
```

Install the Python requirements:

```bash
python -m venv .venv
source .venv/bin/activate
pip install notebook requests
```

On Windows PowerShell:

```powershell
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install notebook requests
```

For the Java extractor, make sure the `org.json` JAR is available on your classpath. For example, if you use the standalone JAR:

```bash
javac -cp ".:json-20240303.jar" FarsNetFactExtractor.java
java -cp ".:json-20240303.jar" FarsNetFactExtractor
```

On Windows:

```powershell
javac -cp ".;json-20240303.jar" FarsNetFactExtractor.java
java -cp ".;json-20240303.jar" FarsNetFactExtractor
```

If you use Maven, add an `org.json` dependency to your project configuration.

## Usage

### 1. Prepare the FarsNet Article JSON

Create or export a JSON file containing FarsNet articles in the expected input format. By default, the Java file is configured to read:

```text
farsnet_json_extracted_10.json
```

You can change the input path, output path, and maximum number of articles in the `main` method of `FarsNetFactExtractor.java`.

Current default configuration:

```java
extractFactsFromJson(
    "farsnet_json_extracted_10.json",
    "facts_output_first50.json",
    50
);
```

### 2. Extract Candidate Facts

Compile and run the Java extractor:

```bash
javac -cp ".:json-20240303.jar" FarsNetFactExtractor.java
java -cp ".:json-20240303.jar" FarsNetFactExtractor
```

This produces a facts file such as:

```text
facts_output_first50.json
```

### 3. Run the Wikipedia Factuality Evaluation

Start Jupyter:

```bash
jupyter notebook farsnet_factuality.ipynb
```

In the notebook, update the input and output paths as needed:

```python
input_file = "facts_output_first50.json"
output_file = "factuality_evaluation_results.json"
max_articles = None  # Use None to process all articles
```

Run the notebook cells to evaluate extracted facts against Persian Wikipedia.

### 4. Analyze and Export Results

After evaluation, use the notebook analysis functions to summarize results and optionally export a CSV file:

```python
summary = analyze_results("factuality_evaluation_results.json")
export_summary_csv(
    "factuality_evaluation_results.json",
    "factuality_summary.csv"
)
```

## How Fact Extraction Works

`FarsNetFactExtractor.java` applies a rule-based extraction approach:

1. Splits Persian text into sentences using punctuation such as `.`, `؟`, and `!`.
2. Removes very short sentences.
3. Keeps sentences that contain factual indicators such as forms of “is”, “has”, “includes”, “born”, “died”, “founded”, “located”, or “known as”.
4. Removes question-like sentences.
5. Cleans citations, URLs, and parenthetical content.
6. Saves up to 10 candidate facts per article.

This approach is simple and transparent, but it may miss valid facts or include some noisy statements.

## How Wikipedia Verification Works

`farsnet_factuality.ipynb` evaluates each extracted fact using Persian Wikipedia evidence. The notebook generally follows this process:

1. Try to find a Persian Wikipedia page matching the article title.
2. Follow redirects or normalized titles when available.
3. If no exact page is found, search Persian Wikipedia for related pages.
4. Retrieve the page summary.
5. Compare each extracted fact against the summary using:

   * exact text matching;
   * keyword overlap;
   * fuzzy sentence similarity.
6. Store the verification status, confidence, evidence, and matched Wikipedia page.

## Configuration Reference

| Component      | Setting                | Description                                                            |
| -------------- | ---------------------- | ---------------------------------------------------------------------- |
| Java extractor | `inputJsonPath`        | Path to the FarsNet article JSON file.                                 |
| Java extractor | `outputJsonPath`       | Path where extracted facts will be saved.                              |
| Java extractor | `maxArticles`          | Maximum number of articles to process.                                 |
| Java extractor | max facts per article  | The extractor currently keeps up to 10 facts per article.              |
| Notebook       | `input_file`           | Path to the extracted facts JSON file.                                 |
| Notebook       | `output_file`          | Path where factuality results will be saved.                           |
| Notebook       | `max_articles`         | Number of articles to evaluate; use `None` for all articles.           |
| Notebook       | Wikipedia search limit | Number of candidate Wikipedia pages considered during fallback search. |

## Interpreting Results

The evaluation output should be interpreted carefully:

* **Verified** means the fact appears to be supported by the retrieved Persian Wikipedia evidence.
* **Unverified** means the notebook did not find enough supporting evidence in the retrieved summary.
* **Low confidence** results should be manually reviewed.
* **No page found** may indicate a title mismatch, missing Wikipedia coverage, or a need for better entity normalization.

## Limitations

* The system relies on Persian Wikipedia summaries, which may omit true facts.
* Wikipedia may contain incomplete, outdated, or contested information.
* Rule-based Persian sentence extraction can miss facts or extract noisy statements.
* Fuzzy matching can produce false positives or false negatives.
* Article title mismatches can reduce verification recall.
* The project currently does not use full Wikipedia article text, Wikidata, or external references.

## Recommended Improvements

Possible future enhancements include:

* Add command-line arguments to `FarsNetFactExtractor.java` for input path, output path, and article limit.
* Add a `requirements.txt` file for Python dependencies.
* Add Maven or Gradle configuration for the Java dependency.
* Add unit tests for sentence extraction, cleaning, and scoring logic.
* Use a Persian NLP library for better sentence splitting and tokenization.
* Cache Wikipedia API responses to reduce repeated requests.
* Compare facts against full Wikipedia article text, not only summaries.
* Add Wikidata-based entity linking and property verification.
* Add manual annotation support for evaluating precision and recall.

## Troubleshooting

### `package org.json does not exist`

The `org.json` library is missing from the Java classpath. Download the JSON JAR or configure the dependency through Maven or Gradle, then compile again with the correct classpath.

### `FileNotFoundException`

Check that the input JSON file exists and that the path in `FarsNetFactExtractor.java` is correct.

### Persian characters appear corrupted

Make sure all JSON files are saved as UTF-8. Avoid opening or saving Persian text files with tools that change the encoding.

### Many facts are reported as unverified

This can happen when:

* the Persian Wikipedia page title differs from the FarsNet article title;
* the fact is true but not included in the Wikipedia summary;
* the fact requires full article evidence rather than summary evidence;
* the extracted statement is too broad, noisy, or ambiguous.

### Wikipedia API requests fail

Check your internet connection and retry later. If processing many articles, consider adding request throttling and response caching.

## Reproducibility Notes

For more reproducible experiments:

* record the date of each Wikipedia evaluation run;
* save the exact input article JSON and extracted facts file;
* keep the generated JSON and CSV outputs together;
* document any changes to matching thresholds or search behavior;
* manually review a sample of verified and unverified facts.

## License

This repository currently does not include an explicit license file. Add a `LICENSE` file before distributing, modifying, or reusing the code outside the repository owner’s intended context.

## Citation

If you use this project in academic work, cite the repository and describe the Wikipedia-backed factuality evaluation method, including any modifications you make to extraction rules, matching thresholds, or evidence sources.

## Acknowledgments

This project uses Persian Wikipedia as an external evidence source for factuality evaluation.
