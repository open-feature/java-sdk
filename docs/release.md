This is a simple checklist when performing a release.

1. Ensure we're up-to-date with the current spec via `./spec_finder.py --diff-output --refresh-spec`
2. Update `README.md` with the current spec that we're up to
3. Update `README.md` to keep the install instructions on the right version
4. Update `<version>` in pom.xml
5. Run `mvn verify` to ensure it all works.
6. Commit the results as the new version.

Then in GitHub, trigger a release.

1. Go to [github releases](https://github.com/open-feature/java-sdk/releases/new)
2. Name a tag w/ the version you want to release (e.g. `0.1.0`)
3. Click the generate release notes button and write some text about what actually changed.
4. Submit
5. Validate the action which builds the result happened correctly.

If something went wrong above, here's how you reset.
1. Save the release notes you wrote.
2. Delete the release.
3. Delete the tag with `git push --delete  origin 0.1.0` where 0.1.0 is your tag name.
4. 