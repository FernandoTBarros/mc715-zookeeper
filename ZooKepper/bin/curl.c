#include <stdio.h>
#include <string.h>
#include <curl/curl.h>
#include <string.h>
#define MAX_BUF	65536

char wr_buf[MAX_BUF+1];
int  wr_index;

/*
 * Write data callback function (called within the context of 
 * curl_easy_perform.
 */
size_t write_data( void *buffer, size_t size, size_t nmemb, void *userp )
{
  int segsize = size * nmemb;

  /* Check to see if this data exceeds the size of our buffer. If so, 
   * set the user-defined context value and return 0 to indicate a
   * problem to curl.
   */
  if ( wr_index + segsize > MAX_BUF ) {
    *(int *)userp = 1;
    return 0;
  }

  /* Copy the data from the curl buffer into our buffer */
  memcpy( (void *)&wr_buf[wr_index], buffer, (size_t)segsize );

  /* Update the write index */
  wr_index += segsize;

  /* Null terminate the buffer */
  wr_buf[wr_index] = 0;

  /* Return the number of bytes received, indicating to curl that all is okay */
  return segsize;
}


/*
 * Simple curl application to read the index.html file from a Web site.
 */
int main(int argc, char *argv[])
{
  CURL *curl;
  CURLcode ret;
  int  wr_error;
  char postData[1024];
  int i=0;
  int con = atoi(argv[3]);
  for(i=0;i<1024;i++)
    postData[i]='\0';
  wr_error = 0;
  wr_index = 0;
  if(con==0)
    strcat(postData,"conectar=1&znode=");
  else
    strcat(postData,"conectar=0&znode=");
  strcat(postData,argv[1]);
  strcat(postData,"&content=");
  strcat(postData,argv[2]);
  /* First step, init curl */
  curl = curl_easy_init();
  if (!curl) {
    printf("couldn't init curl\n");
    return 0;
  }

  /* Tell curl the URL of the file we're going to retrieve */
  curl_easy_setopt( curl, CURLOPT_URL, "aprovacao.dirigente.com.br/mc715/script.php" );

  /* Tell curl that we'll receive data to the function write_data, and
   * also provide it with a context pointer for our error return.
   */
  curl_easy_setopt( curl, CURLOPT_WRITEDATA, (void *)&wr_error );
  curl_easy_setopt( curl, CURLOPT_WRITEFUNCTION, write_data );
  curl_easy_setopt( curl, CURLOPT_POSTFIELDS, postData);

  /* Allow curl to perform the action */
  ret = curl_easy_perform( curl );

  //printf( "ret = %d (write_error = %d)\n", ret, wr_error );

  /* Emit the page if curl indicates that no errors occurred */
  if ( ret == 0 ) printf( "%s\n", wr_buf );

  curl_easy_cleanup( curl );

  return 0;
}
