## Pull Request description

Put your PR description here instead of this sentence.   

## General checklist

- [ ] You have reviewed the guidelines [document](https://docs.google.com/document/d/1wqcOafLx5hth8SAg4dqV_LV3un3m5WYR8RdTJ4MbbUM/edit?usp=sharing).
- [ ] [Labels](https://docs.github.com/en/issues/using-labels-and-milestones-to-track-work/managing-labels#about-labels) that classify your pull request have been added.
- [ ] The [milestone](https://docs.github.com/en/issues/using-labels-and-milestones-to-track-work/about-milestones) is specified and corresponds to fix version.  
- [ ] Description references specific [issue](https://github.com/sobeam/sobeam/issues).
- [ ] Description contains human-readable scope of changes.
- [ ] Description contains brief notes about what needs to be added to the documentation.
- [ ] No merge conflicts, commented blocks of code, code formatting issues.
- [ ] Changes are backward compatible or upgrade script is provided.
- [ ] Similar PR is opened for PE version to simplify merge. Crosslinks between PRs added. Required for internal contributors only.
  
## Front-End feature checklist

- [ ] Screenshots with affected component(s) are added. The best option is to provide 2 screens: before and after changes;
- [ ] If you change the widget or other API, ensure it is backward-compatible or upgrade script is present.
- [ ] Ensure new API is documented [here](https://github.com/sobeam/sobeam-ui-help)

## Back-End feature checklist

- [ ] Added corresponding unit and/or integration test(s). Provide written explanation in the PR description if you have failed to add tests.
- [ ] If new dependency was added: the dependency tree is checked for conflicts.
- [ ] If new service was added: the service is marked with corresponding @TbCoreComponent, @TbRuleEngineComponent, @TbTransportComponent, etc.
- [ ] If new REST API was added: the RestClient.java was updated, issue for [Python REST client](https://github.com/sobeam/sobeam-python-rest-client) is created.
- [ ] If new yml property was added: make sure a description is added (above or near the property).



