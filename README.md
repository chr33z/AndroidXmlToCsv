Andriod Metaphrase
===============

Andriod Metaphrase is a simple java tool that helps preparing android strings for translation. It can either create a csv file from a specified source language or creates multiple xml string files from a given csv table.

###Convert Android XML files to CSV table
#[info] Each string file in your android project has to be in values folder with a valid locale suffix like "values-en" or "values-de". String files within the "values" folder are NOT considered

Start the tool with the following parameters:

```
android-metaphrase --to-csv --project-directory [android project directory] --orig-lang [locale] --target-langs [locale1,locale2,...] --path-csv [absolute path of csv file]"
```

The tool creates a csv table with the columns ```filename, stringname, locale. locale1, locale2, ...```

### Usage csv to xml

```still to be implemented```

