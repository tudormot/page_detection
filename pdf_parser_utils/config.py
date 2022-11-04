
import argparse


parser = argparse.ArgumentParser(description='Parse text from a pdf '
                                             'document, and save it as a '
                                             'json of bucket of words')
parser.add_argument('--pdf_filepath', type=str,
                    help='The path where the pdf resides, in case we are '
                         'parsing pdfs',
                    required=False)
parser.add_argument('--json_dir', type=str,
                    help='The path where the json dir resides, in case we '
                         'are parsing jsons',
                    required=False)
parser.add_argument('--custom_first_page', required=False,
                       help='The page at which the page start getting '
                            'enumerated in the book. The detection will only '
                            'be available starting from this one')
# parser.add_argument('--json_key_path', type=str,
#                     help='The security key required to use the google play '
#                          'store api.',
#                     required=True)


args = parser.parse_args()

