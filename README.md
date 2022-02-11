
# GHSearch Platform

This project is made of two subprojects:
1. `application`: The main application has two main responsibilities:
    1. Crawling GitHub and retrieving repository information. This can be disabled with `app.crawl.enabled` argument.
    2. Serving as the backend server for website/frontend
2. `front-end`: A frontend for searching the database, which is available at http://seart-ghs.si.usi.ch

## Setup & Run Project Locally (for development)

The detailed instruction can be find [here](./README_SETUP.md).


## Dockerisation :whale:
The instruction to deploy the project via Docker is available [here](./README_DEPLOY.md).


## More Info on Flyway and Database Migration
To learn more about Flyway you can read on [here](./README_flyway.md).

---
## FAQ

### How can I report a bug or request a feature or ask a question?**
Please add a [new issue](https://github.com/seart-group/ghs/issues/) and we will get back to you very soon.

### How add a new programming language to platform?
1. Add the new **language name** to `supported_languages` table via:
   1. Flyway migration file (recommended): Create a new file `src/main/resources/db/migration/Vx__NewLangs.sql` containing:
      `INSERT INTO supported_language (name,added) VALUES ('C++',current_timestamp);`
   2. Or, manually editing the table.
   - **Note**: 
     - A comprehensive list of valid languages (and their aliases) are available at [here](https://github.com/github/linguist/blob/master/lib/linguist/languages.yml). 
     - Plus you can see a similar list at [GitHub Advanced Search Page](https://github.com/search/advanced). 
     - You can use the following link to verify if a language is valid, and gives an upper-bound for the number of new repositories to be mined:
       - Example: `C++` -> `C%2B%2B`:`https://api.github.com/search/repositories?q=is:public+stars:%3E10+language:C%2B%2B`. 
2. Add the new **language icon**: See [this guide](./src/main/fe-src/public/icons/README.md)
   

