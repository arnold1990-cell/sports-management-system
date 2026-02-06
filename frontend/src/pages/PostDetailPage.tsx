import React, { useEffect, useState } from 'react';
import { Box, Button, Card, CardContent, TextField, Typography, Alert } from '@mui/material';
import { useParams } from 'react-router-dom';
import api from '../api/client';
import { useAuth } from '../context/AuthContext';

interface Post {
  id: string;
  title: string;
  content: string;
  coverImageUrl?: string;
  authorName?: string;
  createdAt: string;
}

interface Comment {
  id: string;
  content: string;
  authorId?: string;
  authorName?: string;
  createdAt: string;
}

const PostDetailPage: React.FC = () => {
  const { id } = useParams();
  const { accessToken, userId, roles } = useAuth();
  const [post, setPost] = useState<Post | null>(null);
  const [comments, setComments] = useState<Comment[]>([]);
  const [content, setContent] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  const loadPostAndComments = async () => {
    if (!id) {
      setError('Missing post identifier.');
      setLoading(false);
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const [postResponse, commentResponse] = await Promise.all([
        api.get(`/api/posts/published/${id}`),
        api.get(`/api/comments/post/${id}`)
      ]);
      setPost(postResponse.data);
      setComments(commentResponse.data.content || []);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Unable to load post details');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPostAndComments();
  }, [id]);

  const handleAddComment = async () => {
    if (!id) return;
    try {
      await api.post(`/api/comments/post/${id}`, { content });
      setContent('');
      loadPostAndComments();
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Unable to add comment');
    }
  };

  const handleDelete = async (commentId: string) => {
    try {
      await api.delete(`/api/comments/${commentId}`);
      loadPostAndComments();
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Unable to delete comment');
    }
  };

  if (loading) {
    return <Typography>Loading post...</Typography>;
  }

  if (error && !post) {
    return (
      <Box>
        <Alert severity="error">{error}</Alert>
        <Button sx={{ mt: 2 }} variant="contained" onClick={loadPostAndComments}>Retry</Button>
      </Box>
    );
  }

  if (!post) {
    return <Typography>No post available.</Typography>;
  }

  return (
    <Box>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      <Card sx={{ mb: 3 }}>
        {post.coverImageUrl && (
          <Box component="img" src={post.coverImageUrl} alt={post.title} sx={{ width: '100%', height: 260, objectFit: 'cover' }} />
        )}
        <CardContent>
          <Typography variant="h4" gutterBottom>{post.title}</Typography>
          <Typography variant="body2" gutterBottom>By {post.authorName || 'Admin'} on {new Date(post.createdAt).toLocaleDateString()}</Typography>
          <Typography variant="body1" sx={{ whiteSpace: 'pre-wrap' }}>{post.content}</Typography>
        </CardContent>
      </Card>
      <Box>
        <Typography variant="h6" gutterBottom>Comments</Typography>
        {comments.map((comment) => {
          const canDelete = roles.includes('ADMIN') || (userId && comment.authorId === userId);
          return (
            <Card key={comment.id} sx={{ mb: 2 }}>
              <CardContent>
                <Typography variant="subtitle2">{comment.authorName || 'User'} • {new Date(comment.createdAt).toLocaleString()}</Typography>
                <Typography variant="body2">{comment.content}</Typography>
                {canDelete && (
                  <Button size="small" color="error" onClick={() => handleDelete(comment.id)}>Delete</Button>
                )}
              </CardContent>
            </Card>
          );
        })}
        {accessToken ? (
          <Card>
            <CardContent sx={{ display: 'grid', gap: 2 }}>
              {error && <Alert severity="error">{error}</Alert>}
              <TextField label="Add a comment" multiline minRows={3} value={content} onChange={(e) => setContent(e.target.value)} />
              <Button variant="contained" onClick={handleAddComment}>Submit Comment</Button>
            </CardContent>
          </Card>
        ) : (
          <Typography variant="body2">Login to add a comment.</Typography>
        )}
      </Box>
    </Box>
  );
};

export default PostDetailPage;
