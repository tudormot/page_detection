import json
import os
from config import args
from PyPDF2 import PdfReader
import pdfminer

OUTPUT_JSON_FILENAME = "bookText.json"


def parse_pdf():
    assert args.pdf_filepath is not None, "Cant parse a pdf if no pdf " \
                                          "filepath is given"
    reader = PdfReader(args.pdf_filepath)
    number_of_pages = len(reader.pages)
    if args.custom_first_page is not None:
        assert args.custom_first_page < number_of_pages, "What sot of pdf is this? Or is the custom first page argument specified incorrectly?"
        start_page = args.custom_first_page
    else:
        start_page = 1

    output_dict = {}
    for page_nr, page in enumerate(reader.pages):
        output_dict[page_nr + start_page] = page.extract_text()

    with open(OUTPUT_JSON_FILENAME, 'w') as fp:
        json.dump(output_dict, fp, indent=4)

def parse_json_dir():
    assert args.json_dir is not None, "Cant parse a dir containing jsons if " \
                                      "no json dir is given"
    output_dict = {}
    for filename in os.listdir(args.json_dir):
        f = os.path.join(args.json_dir, filename)
        # checking if it is a file
        with open(f) as json_file:
            json_data = json.load(json_file)
            test_list = [cell['text'] + ' ' for cell in json_data['cells']]
            output_dict[filename.split('.json')[0]] = "".join(test_list)
    with open(OUTPUT_JSON_FILENAME, 'w') as fp:
        json.dump(output_dict, fp, indent=4)

    print("We have parsed ", len(output_dict), ' nr of pages.')


if __name__ == '__main__':
    # parse_pdf()
    parse_json_dir()
