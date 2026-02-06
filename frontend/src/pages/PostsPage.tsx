import React, { useEffect, useState } from 'react';
import { Box, Card, CardContent, TextField, Typography, Grid, Button, Alert } from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';
import api from '../api/client';

interface Post {
  id: string;
  title: string;
  content: string;
  coverImageUrl?: string;
  createdAt: string;
}

const PostsPage: React.FC = () => {
  const [posts, setPosts] = useState<Post[]>([]);
  const [keyword, setKeyword] = useState('');
  const [from, setFrom] = useState('');
  const [to, setTo] = useState('');
  const [error, setError] = useState<string | null>(null);

  const loadPosts = async () => {
    const params: any = { page: 0, size: 12 };
    if (keyword) params.keyword = keyword;
    if (from) params.from = new Date(from).toISOString();
    if (to) params.to = new Date(to).toISOString();
    try {
      const response = await api.get('/api/posts/published', { params });
      setPosts(response.data.content || []);
      setError(null);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Unable to load posts');
    }
  };

  useEffect(() => {
    loadPosts();
  }, []);

  return (
    <Box>
      <Typography variant="h4" gutterBottom>Posts & Announcements</Typography>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      <Card sx={{ mb: 3 }}>
        <CardContent sx={{ display: 'grid', gap: 2 }}>
          <Typography variant="h6">Search</Typography>
          <TextField label="Keyword" value={keyword} onChange={(e) => setKeyword(e.target.value)} />
          <TextField type="date" label="From" InputLabelProps={{ shrink: true }} value={from} onChange={(e) => setFrom(e.target.value)} />
          <TextField type="date" label="To" InputLabelProps={{ shrink: true }} value={to} onChange={(e) => setTo(e.target.value)} />
          <Button variant="contained" onClick={loadPosts}>Search</Button>
        </CardContent>
      </Card>
      <Grid container spacing={2}>
        {posts.map((post) => (
          <Grid item xs={12} md={4} key={post.id}>
            <Card component={RouterLink} to={`/posts/${post.id}`}>
              {post.coverImageUrl && (
                <Box component="img" src={post.coverImageUrl} alt={post.title} sx={{ width: '100%', height: 180, objectFit: 'cover' }} />
              )}
              <CardContent>
                <Typography variant="h6">{post.title}</Typography>
                <Typography variant="body2">
                  {post.content.substring(0, 120)}...
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
};

export default PostsPage;
