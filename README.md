
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
  1. See the "Adding C#" commit on December 17th 2020.
  2. Create a new Flyway migration file to insert a new language row on `supported_languages` table.
   

