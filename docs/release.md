# Releases

This repo uses _Release Please_ to release packages. Release Please sets up a running PR that tracks all changes in the library, and maintains the versions according to [conventional commits](https://www.conventionalcommits.org/en/v1.0.0/), generated when [PRs are merged](https://github.com/amannn/action-semantic-pull-request), based on the PR title. The semantics of the PR title are enforced by the `lint-pr.yml` workflow. When Release Please's running PR is merged, a new release is created, and the associated artifacts are published.

## Customization of changelog and release notes. 

If you'd like to add custom content to a release, you can do this by editing the content in a Release Please PR's description. This content will be added to the notes for that release. If you'd like to add content to the changelog, simply push updates to the changelog in the Release Please PR.

## Configuration

The `release-please-config.json` defines the release please configuration. See schema [here](https://github.com/googleapis/release-please/blob/main/schemas/config.json) to understand all the options. We use the "simple" release strategy and annotate the POM with an element to help release please find the correct XML entity to update (the version element). Release Please stores it's understanding of the current version in the `version.txt` file.