# src/video_generator/storage.py

import os
import boto3
from botocore.exceptions import ClientError
from boto3.s3.transfer import S3UploadFailedError
from dotenv import load_dotenv

load_dotenv()

AWS_ACCESS_KEY_ID = os.getenv("AWS_ACCESS_KEY_ID")
AWS_SECRET_ACCESS_KEY = os.getenv("AWS_SECRET_ACCESS_KEY")
AWS_REGION = os.getenv("AWS_REGION")
S3_BUCKET_NAME = os.getenv("S3_BUCKET_NAME")

s3_client = boto3.client(
    "s3",
    aws_access_key_id=AWS_ACCESS_KEY_ID,
    aws_secret_access_key=AWS_SECRET_ACCESS_KEY,
    region_name=AWS_REGION,
)

def upload_video_to_s3(file_path: str, s3_key: str) -> str:
    """
    Upload local file to S3 bucket and return public URL
    """
    try:
        # Remove ACL parameter to avoid AccessControlListNotSupported error
        s3_client.upload_file(file_path, S3_BUCKET_NAME, s3_key)
        url = f"https://{S3_BUCKET_NAME}.s3.{AWS_REGION}.amazonaws.com/{s3_key}"
        return url
    except ClientError as e:
        print(f"Error uploading to S3: {e}")
        raise e

class S3Storage:
    def __init__(self):
        self.s3_client = s3_client
        self.bucket_name = S3_BUCKET_NAME
        self.region = AWS_REGION
    
    def upload_file(self, file_path: str, s3_key: str) -> str:
        """
        Upload local file to S3 bucket and return object URL.
        - Attempts public-read ACL first for easy public access.
        - If ACLs are disabled on the bucket (ObjectOwnership: BucketOwnerEnforced),
          retries without ACL to avoid AccessControlListNotSupported.
        """
        try:
            # First try with public-read for direct public access
            self.s3_client.upload_file(file_path, self.bucket_name, s3_key, ExtraArgs={"ACL": "public-read"})
        except S3UploadFailedError as e:
            # boto3 raises S3UploadFailedError wrapping underlying ClientError
            if "AccessControlListNotSupported" in str(e):
                # Retry without ACL if bucket has ACLs disabled
                self.s3_client.upload_file(file_path, self.bucket_name, s3_key)
            else:
                print(f"Error uploading to S3: {e}")
                raise
        except ClientError as e:
            code = getattr(e, "response", {}).get("Error", {}).get("Code")
            if code == "AccessControlListNotSupported":
                self.s3_client.upload_file(file_path, self.bucket_name, s3_key)
            else:
                print(f"Error uploading to S3: {e}")
                raise
        url = f"https://{self.bucket_name}.s3.{self.region}.amazonaws.com/{s3_key}"
        return url
