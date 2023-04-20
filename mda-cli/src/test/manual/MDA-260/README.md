# MDA-260

See <https://shibboleth.atlassian.net/browse/MDA-260>

Test as follows:

```bash
.../mda.sh config.xml test
```

Result should be something like this:

```
INFO  - Pipeline 'test' execution starting at Thu Apr 20 14:47:34 BST 2023
INFO  - stage1 >>> stage1a, count=0
INFO  - stage1a completed, duration=PT0.000005S
INFO  - stage1 <<< stage1a, count=0, duration=PT0.000184S
INFO  - stage1 >>> stage1b, count=0
INFO  - stage1 <<< stage1b, count=0, duration=PT0.000026S
INFO  - stage1 completed, duration=PT0.000295S
INFO  - stage2 completed, duration=PT0.000002S
INFO  - Pipeline 'test' execution completed at Thu Apr 20 14:47:34 BST 2023; run time 0.009 seconds
```
