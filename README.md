### Java Spring template project

This project is based on a GitLab [Project Template](https://docs.gitlab.com/ee/gitlab-basics/create-project.html).

Improvements can be proposed in the [original project](https://gitlab.com/gitlab-org/project-templates/spring).

### CI/CD with Auto DevOps

This template is compatible with [Auto DevOps](https://docs.gitlab.com/ee/topics/autodevops/).

If Auto DevOps is not already enabled for this project, you can [turn it on](https://docs.gitlab.com/ee/topics/autodevops/#enabling-auto-devops) in the project settings.

###Maven Commands
mvn spring-boot:run

mvn spring-boot:run -Dspring-boot.run.arguments=--app.crawl.enabled=true,--app.crawl.interval=2009-01-01T00:00:00..2010-01-01T00:00:00