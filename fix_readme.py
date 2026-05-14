#!/usr/bin/env python3
#!/usr/bin/env python3
# Fix README - update F-Droid sections with clear setup instructions

with open('/home/bubo/.hermes/workspace/Code/VoidScanner/README.md', 'r') as f:
    content = f.read()
    lines = content.split('\n')

print(len(lines), "lines in README")