import json
import os
from config import args
from PyPDF2 import PdfReader
import shutil

OUTPUT_JSON_FILENAME = "bookText.json"
OUTPUT_PDF_SUBSET_DIR = "/home/tudor/Workspace/page_detection" \
                       "/pdf_parser_utils/pdf_subset"
INPUT_PDF_FILE = "/home/tudor/Workspace/page_detection/pdf_parser_utils/data/PDF"
NR_PAGES_TO_BE_PROCESSED = 200


def parse_pdf():
    assert args.pdf_filepath is not None, "Cant parse a pdf if no pdf " \
                                          "filepath is given"
    reader = PdfReader(args.pdf_filepath)
    number_of_pages = len(reader.pages)
    if args.custom_first_page is not None:
        assert args.custom_first_page < number_of_pages, "What sort of pdf " \
                                                         "is this? Or is the custom first page argument specified incorrectly?"
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

def advanced_parse_json_dir():
    assert args.json_dir is not None, "Cant parse a dir containing jsons if " \
                                      "no json dir is given"

    output_dict = {}
    for i, filename in enumerate(os.listdir(args.json_dir)):
        if i == NR_PAGES_TO_BE_PROCESSED:
            break
        document_id = filename.split('.json')[0]
        f = os.path.join(args.json_dir, filename)
        with open(f) as json_file:
            json_data = json.load(json_file)
            test_list = [cell['text'] + ' ' for cell in json_data['cells']]
            output_dict[i] = "".join(test_list)
        shutil.copy2(INPUT_PDF_FILE + '/' + document_id + '.pdf',
                     OUTPUT_PDF_SUBSET_DIR + '/' + str(i) + '.pdf'
                     )

    with open(OUTPUT_JSON_FILENAME, 'w') as fp:
        json.dump(output_dict, fp, indent=4)

    print("We have parsed ", len(output_dict), ' nr of pages.')


if __name__ == '__main__':
    # parse_pdf()
    advanced_parse_json_dir()
