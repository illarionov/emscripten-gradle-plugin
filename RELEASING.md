# Releasing

1. Change the version in `emscripten-plugin/build.gradle` to a non-SNAPSHOT version.
2. Update the `CHANGELOG.md` for the impending release.
3. Update the `README.md` and `doc/aggregate-documentation/FRONTPAGE.md` with the new version.
4. `git commit -am "Prepare for release X.Y.Z."` (where X.Y.Z is the new version)
5. `git tag -a X.Y.Z -m "Version X.Y.Z"` (where X.Y.Z is the new version)
6. `git push origin && git push --tags`
7. Merge pull-request and trigger publish workflow in GitHub Actions
8. Update the `plugin/build.gradle` to the next SNAPSHOT version.
9. `git commit -am "Prepare next development version."`
10. `git push`
