import React, { useEffect, useState } from 'react';
import { Box, Button, Card, CardContent, TextField, Typography, Grid, Alert, MenuItem } from '@mui/material';
import api from '../api/client';

interface Post {
  id: string;
  title: string;
  content: string;
  coverImageUrl?: string;
  status: string;
}

const AdminPostsPage: React.FC = () => {
  const [posts, setPosts] = useState<Post[]>([]);
  const [form, setForm] = useState({ title: '', content: '', coverImageUrl: '', status: 'DRAFT' });
  const [file, setFile] = useState<File | null>(null);
  const [error, setError] = useState<string | null>(null);

  const loadPosts = async () => {
    try {
      const response = await api.get('/api/posts');
      setPosts(response.data.content || []);
      setError(null);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Unable to load posts');
    }
  };

  useEffect(() => {
    loadPosts();
  }, []);

  const uploadImage = async () => {
    if (!file) return;
    const formData = new FormData();
    formData.append('file', file);
    try {
      const response = await api.post('/api/uploads', formData, { headers: { 'Content-Type': 'multipart/form-data' } });
      setForm((prev) => ({ ...prev, coverImageUrl: response.data.url }));
      setError(null);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Unable to upload image');
    }
  };

  const savePost = async () => {
    try {
      await api.post('/api/posts', form);
      setForm({ title: '', content: '', coverImageUrl: '', status: 'DRAFT' });
      setFile(null);
      loadPosts();
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Unable to save post');
    }
  };

  const togglePublish = async (postId: string, publish: boolean) => {
    try {
      await api.patch(`/api/posts/${postId}/publish`, null, { params: { publish } });
      loadPosts();
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Unable to update publish status');
    }
  };

  const deletePost = async (postId: string) => {
    try {
      await api.delete(`/api/posts/${postId}`);
      loadPosts();
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Unable to delete post');
    }
  };

  return (
    <Box>
      <Typography variant="h4" gutterBottom>Admin Posts</Typography>
      {error && <Alert severity="error">{error}</Alert>}
      <Card sx={{ mb: 3 }}>
        <CardContent sx={{ display: 'grid', gap: 2 }}>
          <Typography variant="h6">Create Post</Typography>
          <TextField label="Title" value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} />
          <TextField label="Content" multiline minRows={4} value={form.content} onChange={(e) => setForm({ ...form, content: e.target.value })} />
          <TextField label="Cover Image URL" value={form.coverImageUrl} onChange={(e) => setForm({ ...form, coverImageUrl: e.target.value })} />
          <TextField type="file" inputProps={{ accept: 'image/png,image/jpeg,image/webp' }} onChange={(e) => setFile(e.target.files ? e.target.files[0] : null)} />
          <Button variant="outlined" onClick={uploadImage} disabled={!file}>Upload Image</Button>
          <TextField select label="Status" value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
            {['DRAFT', 'PUBLISHED'].map((status) => (
              <MenuItem key={status} value={status}>{status}</MenuItem>
            ))}
          </TextField>
          <Button variant="contained" onClick={savePost}>Save Post</Button>
        </CardContent>
      </Card>
      <Grid container spacing={2}>
        {posts.map((post) => (
          <Grid item xs={12} md={4} key={post.id}>
            <Card>
              {post.coverImageUrl && (
                <Box component="img" src={post.coverImageUrl} alt={post.title} sx={{ width: '100%', height: 180, objectFit: 'cover' }} />
              )}
              <CardContent>
                <Typography variant="h6">{post.title}</Typography>
                <Typography variant="body2">Status: {post.status}</Typography>
                <Box sx={{ display: 'flex', gap: 1, mt: 2 }}>
                  <Button size="small" variant="outlined" onClick={() => togglePublish(post.id, post.status !== 'PUBLISHED')}>
                    {post.status === 'PUBLISHED' ? 'Unpublish' : 'Publish'}
                  </Button>
                  <Button size="small" color="error" onClick={() => deletePost(post.id)}>Delete</Button>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
};

export default AdminPostsPage;
