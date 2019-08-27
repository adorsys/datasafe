# Datasafe long run test results

Tests have been run on 6 different aws ec2 instances:

- m5.large (2 CPU, 8GB RAM);
- m5.xlarge (4 CPU, 16GB RAM);
- m5.2xlarge (8 CPU, 32GB RAM);
- c5n.large (2 CPU, 5.25GB RAM);
- c5n.xlarge (4 CPU, 10.5GB RAM);
- c5n.2xlarge (8 CPU, 21GB RAM).

GP on charts stands for General Purpose (m5) instances
CO - Compute Optimized (c5n).

On each instance test has been run 3 days, 3 times per day with 2, 4 and 8 threads and 100kb, 1mb, 10mb file size.
JVM was launched with -Xmx256m

For testing multi-bucket performance 6 aws s3 buckets were created.
Multi-bucket performance was tested only on c5n.2xlarge instance with using 2, 4, 6 buckets.

WRITE operation

![](.images/Write.png)

2. READ operation

![](.images/Read.png)

3. SHARE operation

![](.images/Share.png)

4. LIST operation

![](.images/List.png)

5. DELETE Operation

![](.images/Delete.png)

Single bucket ALL-IN-ONE Chart

![](.images/SinglebucketGP.png)

![](.images/SinglebucketCO.png)


MULTIBUCKET TEST

![](.images/Multibucket.png)

All tests were made using AES256_CBC Encryption algorithm. On next chart test results using AES256_GCM comparing to AES256_CBC.
![](.images/CBCvsGCM.png)