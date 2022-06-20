import urllib.request
import json
import re
import difflib
import os
import sys

def _demarkdown(t):
    return t.replace('**', '').replace('`', '').replace('"', '')

def get_spec(force_refresh=False):
    spec_path = './specification.json'
    data = ""
    if os.path.exists(spec_path):
        with open(spec_path) as f:
            data = ''.join(f.readlines())
    else:
        # TODO: Status code check
        spec_response = urllib.request.urlopen('https://raw.githubusercontent.com/open-feature/spec/main/specification.json')
        raw = []
        for i in spec_response.readlines():
            raw.append(i.decode('utf-8'))
        data = ''.join(raw)
        with open(spec_path, 'w') as f:
            f.write(data)
    return json.loads(data)


def main(refresh_spec=False, diff_output=False, limit_numbers=None):
    actual_spec = get_spec(refresh_spec)

    spec_map = {}
    for entry in actual_spec['rules']:
        number = re.search('[\d.]+', entry['id']).group()
        if 'requirement' in entry['machine_id']:
            spec_map[number] = _demarkdown(entry['content'])

        if len(entry['children']) > 0:
            for ch in entry['children']:
                number = re.search('[\d.]+', ch['id']).group()
                if 'requirement' in ch['machine_id']:
                    spec_map[number] = _demarkdown(ch['content'])

    java_specs = {}
    missing = set(spec_map.keys())


    import os
    for root, dirs, files in os.walk(".", topdown=False):
        for name in files:
            F = os.path.join(root, name)
            if '.java' not in name:
                continue
            with open(F) as f:
                data = ''.join(f.readlines())

            for match in re.findall('@Specification\((?P<innards>.*?)"\)', data.replace('\n', ''), re.MULTILINE | re.DOTALL):
                number = re.findall('number\s*=\s*"(.*?)"', match)[0]

                if number in missing:
                    missing.remove(number)
                text_with_concat_chars = re.findall('text\s*=\s*(.*)', match)
                try:
                    # We have to match for ") to capture text with parens inside, so we add the trailing " back in.
                    text = _demarkdown(eval(''.join(text_with_concat_chars) + '"'))
                    entry = java_specs[number] = {
                        'number': number,
                        'text': text,
                    }
                except:
                    print(f"Skipping {match} b/c we couldn't parse it")

    bad_num = len(missing)
    for number, entry in java_specs.items():
        if limit_numbers is not None and len(limit_numbers) > 0 and number not in limit_numbers:
            continue
        if number in spec_map:
            txt = entry['text']
            if txt == spec_map[number]:
                # print(f'{number} is good')
                continue
            else:
                print(f"{number} is bad")
                bad_num += 1
                if diff_output:
                    print(number + '\n' + '\n'.join([li for li in difflib.ndiff([txt], [spec_map[number]]) if not li.startswith(' ')]))
                continue

        print(f"Couldn't find the number {number}")
        print("\n\n")

    if len(missing) > 0:
        print('Missing: ', missing)

    sys.exit(bad_num)


if __name__ == '__main__':
    import argparse

    parser = argparse.ArgumentParser(description='Parse the spec to make sure our tests cover it')
    parser.add_argument('--refresh-spec', action='store_true', help='Re-downloads the spec')
    parser.add_argument('--diff-output', action='store_true', help='print the text differences')
    parser.add_argument('specific_numbers', metavar='num', type=str, nargs='*',
                        help='limit this to specific numbers')

    args = parser.parse_args()
    main(refresh_spec=args.refresh_spec, diff_output=args.diff_output, limit_numbers=args.specific_numbers)
